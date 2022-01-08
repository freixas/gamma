/*
 * Copyright (C) 2021 Antonio Freixas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received first copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gamma.parser;

import gamma.ProgrammingException;
import gamma.execution.hcode.*;
import gamma.value.Coordinate;
import gamma.value.Frame;
import gamma.value.Interval;
import gamma.value.Line;
import gamma.value.PropertyList;
import gamma.value.WorldlineSegment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author Antonio Freixas
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class Parser
{
    class Op
    {
        static private boolean initialized = false;

        final String operator;
        final boolean isLeftAssoc;
        final boolean isBinary;
        final int precedence;

        static HashMap<String, Op> binary = new HashMap<>();
        static HashMap<String, Op> unary = new HashMap<>();

        @SuppressWarnings("LeakingThisInConstructor")
        Op(String operator, boolean isLeftAssoc, boolean isBinary, int precedence)
        {
            this.operator = operator;
            this.isLeftAssoc = isLeftAssoc;
            this.isBinary = isBinary;
            this.precedence = precedence;

            // Store each operator in first table so that we can re-use the
            // instances

            if (isBinary) {
                binary.put(operator, this);
            }
            else {
                unary.put(operator, this);
            }

            initialized = true;
        }

        /**
         * A factory method for creating operators. Given an operator string,
         * it finds and returns a matching operator.
         *
         * @param chr
         * @return
         */
        static Op find(String operator, boolean isBinary)
        {
            Op result;
            if (isBinary) {
                result = binary.get(operator);
            }
            else {
                result = unary.get(operator);
            }
            if (result == null) {
                throw new RuntimeException("Op.find failed to find chr '" + operator + "' in " + (isBinary ? "binary" : "unary") + " table");
            }
            return result;
        }

        /**
         * Return true if at least one Op has been created.
         *
         * @return True if at least one Op has been created
         */
        static boolean isInitialized()
        {
            return initialized;
        }

        @Override
        public String toString()
        {
            return "Op{" + "operator=" + operator + '}';
        }
    }

    class OpToken<T> extends Token<T>
    {
        private final Token<T> token;
        private final Op op;
        private int id;

        OpToken(Token<T> token, Op op)
        {
            super(token.getType(), token.getValue(), token.getFile(), token.getLineNumber(), token.getCharNumber());
            this.token = token;
            this.op = op;
            this.id = -1;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        @Override
        public String toString()
        {
            return "OpToken{" + "token=" + token + ", op=" + op + '}';
        }

    }

    class Pair<A, B>
    {
        public final A first;
        public final B second;

        Pair (A first, B second)
        {
            this.first = first;
            this.second = second;
        }
    }

    private final File file;
    private final String script;
    private ArrayList<Token<?>> tokens;
    private LinkedList<Object> hCodes;

    private boolean animationStatementIsPresent;
    private boolean animationVariableIsPresent;
    private boolean displayVariableIsPresent;

    private int labelId;
    private LinkedList<Pair<Label, Label>> loopLabels;

    private SetStatement setStatement;

    private final Token<?> dummyToken = new Token<>(Token.Type.DELIMITER, '~', null, 0, 0);

    private int tokenPtr;
    private Token<?> curToken;
    private Token<?> peek;

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public Parser(File file, String script)
    {
        this.file = file;
        this.script = script;
        this.hCodes = new LinkedList<>();

        // We only need to initialize the opcode tables once.

        if (!Op.isInitialized()) {
            new Op("<-", true,  true,  8);
            new Op("->", true,  true,  8);

            new Op("||", true,  true,  9);
            new Op("&&", true,  true,  10);

            new Op("==", true,  true,  11);
            new Op("!=", true,  true,  11);

            new Op("<",  true,  true,  12);
            new Op(">",  true,  true,  12);
            new Op("<=", true,  true,  12);
            new Op(">=", true,  true,  12);

            new Op("+",  true,  true,  13);
            new Op("-",  true,  true,  13);

            new Op("*",  true,  true,  14);
            new Op("/",  true,  true,  14);
            new Op("%",  true,  true,  14);

            new Op("^",  false, true,  15);

            new Op("!",  false, false, 16);
            new Op("+",  false, false, 16);
            new Op("-",  false, false, 16);

            new Op(".",  true,  true,  20);

            new Op("(",  false, true,  21);
            new Op("FUNC",  true,  true,  1000); // Used for function names
        }
    }

    /**
     * Get the tokens produced by parsing.
     *
     * @return The tokens produced by parsing.
     */
    public ArrayList<Token<?>> getTokens()
    {
        return this.tokens;
    }

    /**
     * Get the hCodes produced by parsing.
     *
     * @return The hCodes produced by parsing.
     */
    public LinkedList<Object> getHCodes()
    {
        return this.hCodes;
    }

    /**
     * Returns true if the "animation" command was seen.
     *
     * @return True if the "animation" command was seen.
     */
    public boolean isAnimated()
    {
        return animationStatementIsPresent && animationVariableIsPresent;
    }

    /**
     * Returns true if display variables exist.
     *
     * @return True if the "animation" command was seen.
     */
    public boolean hasDisplayVariables()
    {
        return displayVariableIsPresent;
    }

    /**
     * Get the set statement. This contains information which needs to be
     * processed before the HCodeEngine runs.
     *
     * @return The set statement.
     */
    public SetStatement getSetStatement()
    {
        return setStatement;
    }

    /**
     * Parse the script
     * @return
     * @throws ParseException
     */
    public LinkedList<Object> parse() throws ParseException
    {
        animationStatementIsPresent = false;
        animationVariableIsPresent = false;
        displayVariableIsPresent = false;

        loopLabels = new LinkedList<>();

        setStatement = new SetStatement();

        labelId = 0;

        Tokenizer tokenizer = new Tokenizer(file, script);
        tokens = tokenizer.tokenize();

        // tokenPtr points to the current token.
        // When we start, it points one before the start of the token list.
        // The current token is first dummy token.
        // Peek is the token after the current one.
        // It starts out as the first item on the token list.
        // The token list always has at least one item. The last token is
        // always the EOF token.

        setCurrentTokenTo(-1);

        hCodes = parseProgram();
        return hCodes;
    }

    // Generally, each parse method should assume it should start processing
    // the current token. Each parse statement should return with the current
    // token set to the token after whatever syntax it covers.

    private LinkedList<Object> parseProgram() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        nextToken();
        while (!isEOF()) {
            codes.addAll(parseStatementBlock());
        }

        return codes;
    }

    private LinkedList<Object> parseStatementBlock() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        while (!isEOF() && !(isDelimiter() && getChar() == '}')) {

            // A statement block can be first statement block within braces

            if (isDelimiter() && getChar() == '{') {
                nextToken();
                codes.addAll(parseStatementBlock());
                if (!isDelimiter() || getChar() != '}') {
                    throwParseException("Expected a '}'");
                }
                nextToken();
            }

            // Or first statement

            else {
                codes.addAll(parseStatement());
            }
        }

        return codes;
    }

    private LinkedList<Object> parseStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // A statement that starts with first name

        if (isName()) {
            codes.add(new LineInfoHCode(curToken.getFile(), curToken.getLineNumber()));

            // Look far ahead to see if we might have first plain assigment statement

            int curState = tokenPtr;
            boolean isLeftVariable;
            try {
                codes.addAll(parseLeftVariable());
                isLeftVariable = isDelimiter() && getChar() == '=';
            }
            catch (ParseException e) {
                isLeftVariable = false;
            }

            // If we have a left variable, we have a plain assignment
            // statement

            if (isLeftVariable) {
                codes.addAll(parseSemicolonStatement(true));
            }

            // Otherwise, we have something else

            else {
                codes.clear();
                setCurrentTokenTo(curState);

                switch (getString()) {
                    case "if" -> codes.addAll(parseIfStatement());
                    case "while" -> codes.addAll(parseWhileStatement());
                    case "for" -> codes.addAll(parseForStatement());
                    default -> codes.addAll(parseSemicolonStatement(false));
                }
            }
        }

        // A statement that is first block

        else if (isDelimiter() && getChar() == '{') {
            nextToken();
            codes.addAll(parseStatementBlock());
            if (!isDelimiter() || getChar() != '}') {
                throwParseException("Expected a '}'");
            }
            nextToken();
        }

        // Empty statement

        else if (isDelimiter() && getChar() == ';') {
            nextToken();
        }

        // Neither: error

        else {
            throwParseException("Expected the start of a statement");
        }

        return codes;
    }

    private LinkedList<Object> parseSemicolonStatement(boolean leftVariableDetected) throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        codes.addAll(parseSimpleStatement(leftVariableDetected));

        if (!isDelimiter() || getChar() != ';') {
            throwParseException("Expected a ';'");
        }
        nextToken();

        return codes;
    }

    private LinkedList<Object> parseSimpleStatement(boolean leftVariableDetected) throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // If a left variable was detected, we have a normal assignment statement

        if (leftVariableDetected) {
            codes.addAll(parseAssignmentStatement());
        }

        else if (isName()) {
            switch (getString()) {
                case "include" -> parseIncludeStatement();
                case "stylesheet" -> parseStylesheetStatement();
                case "set" -> parseSetStatement();
                case "print" -> codes.addAll(parsePrintStatement());
                case "animate" -> codes.addAll(parseAnimationAssignmentStatement());
                case "range" -> codes.addAll(parseRangeAssignmentStatement());
                case "toggle" -> codes.addAll(parseToggleAssignmentStatement());
                case "choice" -> codes.addAll(parseChoiceAssignmentStatement());
                case "break" -> codes.addAll(parseBreakStatement());
                case "continue" -> codes.addAll(parseContinueStatement());
                case "style" -> codes.addAll(parseStyleStatement());
                default -> codes.addAll(parseCommandStatement());
            }
        }

        return codes;
    }

    private LinkedList<Object> parseIfStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing only that the current token points to "if"

        nextToken();
        if (!isDelimiter() || getChar() != '(') {
            throwParseException("Expected '('");
        }

        // Add the conditional expression

        nextToken();
        codes.addAll(parseExpr());

        if (!isDelimiter() || getChar() != ')') {
            throwParseException("Expected ')'");
        }

        nextToken();
        LinkedList<Object> ifCodes = parseStatement();
        LinkedList<Object> elseCodes = new LinkedList<>();


        if (isName() && getString().equals("else")) {
            nextToken();
            elseCodes = parseStatement();
        }

        Label labelDone = new Label(labelId++);
        Label labelElse = labelDone;
        if (elseCodes.size() > 0) labelElse = new Label(labelId++);

        // Add the test for the conditional expression. If false, this
        // jumps to the else clause (if any) or to the next statement. If
        // true, it falls through to the if clause

        codes.add(new JumpIfFalseHCode(labelElse.getId()));

        // Add the if true clause

        codes.addAll(ifCodes);

        // If we have an else clause, we need to let the if clause jump to
        // the next statement. Then we add the label that marks the start of
        // the else clause and add the else clause code

        if (elseCodes.size() > 0) {
            codes.add(new JumpHCode(labelDone.getId()));
            codes.add(labelElse);
            codes.addAll(elseCodes);
        }

        // Finally we label the end of the entire if statement

        codes.add(labelDone);

        return codes;
    }

    private LinkedList<Object> parseWhileStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        Label labelStart = new Label(labelId++);
        Label labelDone = new Label(labelId++);

        loopLabels.push(new Pair<>(labelStart, labelDone));

        // We start knowing only that the current token points to "while"

        nextToken();
        if (!isDelimiter() || getChar() != '(') {
            throwParseException("Expected '('");
        }

        // Add the conditional expression

        nextToken();
        codes.add(labelStart);
        codes.addAll(parseExpr());

        if (!isDelimiter() || getChar() != ')') {
            throwParseException("Expected ')'");
        }

        nextToken();

        codes.add(new JumpIfFalseHCode(labelDone.getId()));

        codes.addAll(parseStatement());
        codes.add(new JumpHCode(labelStart.getId()));
        codes.add(labelDone);

        loopLabels.pop();

        return codes;
    }

    private LinkedList<Object> parseForStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        Label labelStart = new Label(labelId++);
        Label labelContinue = new Label(labelId++);
        Label labelDone = new Label(labelId++);

        loopLabels.push(new Pair<>(labelContinue, labelDone));

        // We start knowing only that the current token points to "for"

        nextToken();

        // Variable to loop over

        if (!isName()) {
            throwParseException("Expected a variable");
        }
        String loopVariable = curToken.getString();
        nextToken();

        // Initial value

        if (!isDelimiter() || getChar() != '=') {
            throwParseException("Expected '='");
        }
        nextToken();
        LinkedList<Object> initialValue = parseExpr();

        // Final value

        if (!isName() || !getString().equals("to")) {
            throwParseException("Expected 'to'");
        }
        nextToken();
        LinkedList<Object> finalValue = parseExpr();

        // Step value

        if (!isName() || !getString().equals("step")) {
            throwParseException("Expected 'step'");
        }
        nextToken();
        LinkedList<Object> stepValue = parseExpr();

        // Code to loop over

        LinkedList<Object> forLoop = parseStatement();

        // Initialize the loop variable

        codes.add(loopVariable);
        codes.add(new GenericHCode(HCode.Type.FETCH_ADDRESS));
        codes.addAll(initialValue);
        codes.add(new AssignHCode());

        // Calculate and save the final and stepConstant values

        String finalVariable = loopVariable + "$$final";
        String stepVariable = loopVariable + "$$step";

        boolean finalIsConstant = (finalValue.size() == 1) && (finalValue.get(0) instanceof Double);
        Double finalConstant = 1.0;
        if (finalIsConstant) {
            finalConstant = (Double)finalValue.get(0);
        }
        else {
            codes.add(finalVariable);
            codes.add(new GenericHCode(HCode.Type.FETCH_ADDRESS));
            codes.addAll(finalValue);
            codes.add(new AssignHCode());
        }

        boolean stepIsConstant = (stepValue.size() == 1) && (stepValue.get(0) instanceof Double);
        Double stepConstant = 1.0;
        if (stepIsConstant) {
            stepConstant = (Double)stepValue.get(0);
        }
        else {
            codes.add(stepVariable);
            codes.add(new GenericHCode(HCode.Type.FETCH_ADDRESS));
            codes.addAll(stepValue);
            codes.add(new AssignHCode());
        }

        // Test: if (loopVariable$$step == 0) exit

        if (!(stepIsConstant && (double)stepValue.get(0) == 0.0)) {

            // Alternative code when the stepConstant value is not a constant

            if (!stepIsConstant) {
                codes.add(stepVariable);
                codes.add(new GenericHCode(HCode.Type.FETCH));
                codes.add(0.0);
                codes.add(new GenericHCode(HCode.Type.EQ));
                codes.add(new JumpIfTrueHCode(labelDone.getId()));
            }

            codes.add(labelStart);

            // Test: if (loopVariable$$step > 0 && loopVariable > loopVariable$$final) ||
            //          (loopVariable$$step < 0 && loopVariable < loopVariable$$final)) exit

            // If the step value is a constant, we only need to do one comparison

            if (stepIsConstant) {
                if (stepConstant > 0) {
                    codes.add(loopVariable);
                    codes.add(new GenericHCode(HCode.Type.FETCH));

                    if (finalIsConstant) {
                        codes.add(finalConstant);
                    }
                    else {
                        codes.add(finalVariable);
                        codes.add(new GenericHCode(HCode.Type.FETCH));
                    }
                    codes.add(new GenericHCode(HCode.Type.LE));
                    codes.add(new JumpIfFalseHCode(labelDone.getId()));
                }
                else if (stepConstant < 0) {
                    codes.add(loopVariable);
                    codes.add(new GenericHCode(HCode.Type.FETCH));

                    if (finalIsConstant) {
                        codes.add(finalConstant);
                    }
                    else {
                        codes.add(finalVariable);
                        codes.add(new GenericHCode(HCode.Type.FETCH));
                    }
                    codes.add(new GenericHCode(HCode.Type.GE));
                    codes.add(new JumpIfFalseHCode(labelDone.getId()));
                }
            }
            else {
                Label label1 = new Label(labelId++);
                Label label2 = new Label(labelId++);
                Label label3 = new Label(labelId++);

                codes.add(stepVariable);
                codes.add(new GenericHCode(HCode.Type.FETCH));
                codes.add(0.0);
                codes.add(new GenericHCode(HCode.Type.GT));
                codes.add(new JumpAndHCode(label1.getId()));

                codes.add(loopVariable);
                codes.add(new GenericHCode(HCode.Type.FETCH));

                if (finalIsConstant) {
                    codes.add(finalConstant);
                }
                else {
                    codes.add(finalVariable);
                    codes.add(new GenericHCode(HCode.Type.FETCH));
                }
                codes.add(new GenericHCode(HCode.Type.LE));
                codes.add(new GenericHCode(HCode.Type.AND));
                codes.add(label1);
                codes.add(new JumpOrHCode(label2.getId()));

                codes.add(stepVariable);
                codes.add(new GenericHCode(HCode.Type.FETCH));
                codes.add(0.0);
                codes.add(new GenericHCode(HCode.Type.LT));
                codes.add(new JumpAndHCode(label3.getId()));

                codes.add(loopVariable);
                codes.add(new GenericHCode(HCode.Type.FETCH));

                if (finalIsConstant) {
                    codes.add(finalConstant);
                }
                else {
                    codes.add(finalVariable);
                    codes.add(new GenericHCode(HCode.Type.FETCH));
                }
                codes.add(new GenericHCode(HCode.Type.GE));
                codes.add(new GenericHCode(HCode.Type.AND));
                codes.add(label3);
                codes.add(new GenericHCode(HCode.Type.OR));
                codes.add(label2);
                codes.add(new JumpIfFalseHCode(labelDone.getId()));
            }

            // Execute the body

            codes.addAll(forLoop);

            // Increment: loopVariable = loopVariable + loopVariable$$step

            codes.add(labelContinue);

            codes.add(loopVariable);
            codes.add(new GenericHCode(HCode.Type.FETCH_ADDRESS));
            codes.add(loopVariable);
            codes.add(new GenericHCode(HCode.Type.FETCH));
            if (stepIsConstant) {
                codes.add(stepConstant);
            }
            else {
                codes.add(stepVariable);
                codes.add(new GenericHCode(HCode.Type.FETCH));
            }
            codes.add(new AddHCode());
            codes.add(new AssignHCode());

            // Jump back to the start of the loop

            codes.add(new JumpHCode(labelStart.getId()));

            // Label for exit

            codes.add(labelDone);
        }

        loopLabels.pop();

        return codes;
    }

    private LinkedList<Object> parseBreakStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

         // We start knowing that the current token is "break"

        nextToken();
        if (loopLabels.size() < 1) {
            throwParseException("The break statement is not inside a loop");
        }
        Pair<Label, Label> pair = loopLabels.get(0);

        codes.add(new JumpHCode(pair.second.getId()));

        return codes;
    }

    private LinkedList<Object> parseContinueStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

         // We start knowing that the current token is "break"

        nextToken();
        if (loopLabels.size() < 1) {
            throwParseException("The continue statement is not inside a loop");
        }
        Pair<Label, Label> pair = loopLabels.get(0);

        codes.add(new JumpHCode(pair.first.getId()));

        return codes;
    }

    private void parseIncludeStatement() throws ParseException
    {
        // We start knowing that the current token is "include"

        nextToken();

        // The next token has to be first string. We don't have the ability to
        // process expressions in the parser

        if (!isString()) {
            throwParseException("Missing include file name");
        }

        // If the next token isn't first ';', return an empty codes list

        if (!peek.isDelimiter() || peek.getChar() != ';') {
            nextToken();
            return;
        }

        try {
            String name = getString();
            File includeFile = new File(name);
            if (!includeFile.isAbsolute()) {
                includeFile = new File(file.getParent(), name);
            }
            if (!file.exists()) {
                throwParseException("File '" + includeFile.toString() +"' does not exist.");
            }
            if (file.isDirectory()) {
                throwParseException("File '" + includeFile.toString() +"' is a directory.");
            }
            String includeScript = Files.readString(includeFile.toPath());
            Tokenizer tokenizer = new Tokenizer(includeFile, includeScript);
            ArrayList<Token<?>> includeTokens = tokenizer.tokenize();

            // tokenPtr points to the current token, the include file name.
            // tokenPtr - 1 points to "include"
            // tokenPtr + 1 points to the ';'
            // We want to remove the "include" and the name, but not the ';'

            tokens.subList(tokenPtr - 1, tokenPtr + 1).clear();

            // Remove the EOF at the end of the included tokens

            includeTokens.remove(includeTokens.size() - 1);

            // Add all the new stuff after the ';', which is now at tokenPtr - 1

            tokens.addAll(tokenPtr, includeTokens);

            // Reset things so that the current token is ';'

            setCurrentTokenTo(tokenPtr - 1);
        }
        catch (IOException e) {
            throwParseException("IO Error - " + e.getMessage());
        }
    }

    private LinkedList<Object> parseStylesheetStatement() throws ParseException
    {
        // We start knowing that the current token is "stylesheet"

        nextToken();

        return null;
    }

    private void parseSetStatement() throws ParseException
    {
        // We start knowing that the current token is "set"

        nextToken();

        boolean foundUnits = false;
        boolean foundDisplayPrecision = false;
        boolean foundPrintPrecision = false;

        double units = SetStatement.DEFAULT_UNITS;
        double displayPrecision = SetStatement.DEFAULT_DISPLAY_PRECISION;
        double printPrecision = SetStatement.DEFAULT_PRINT_PRECISION;

        while (true) {
            if (!isName()) {
                throwParseException("Expected 'units', 'displayPrecision', or 'printPrecision'");
            }

            // Look for units

            switch (getString()) {
                case "units" -> {
                    if (foundUnits) {
                        throwParseException("Units are set twice");
                    }
                    nextToken();
                    if (!isNumber()) {
                        throwParseException("Units must be set to a floating point number >= 0");
                    }   units = getNumber();
                    nextToken();
                    foundUnits = true;
                }
                case "displayPrecision" -> {
                    if (foundDisplayPrecision) {
                        throwParseException("Display precision is set twice");
                    }
                    nextToken();
                    if (!isNumber()) {
                        throwParseException("Display precision must be set to a floating point number >= 0");
                    }   displayPrecision = getNumber();
                    nextToken();
                    foundDisplayPrecision = true;
                }
                case "printPrecision" -> {
                    if (foundPrintPrecision) {
                        throwParseException("Print precision is set twice");
                    }
                    nextToken();
                    if (!isNumber()) {
                        throwParseException("Print precision must be set to a floating point number >= 0");
                    }   printPrecision = getNumber();
                    nextToken();
                    foundPrintPrecision = true;
                }
                default -> {
                    throwParseException("Expected 'units', 'displayPrecision', or 'printPrecision'");
                }
            }

            // We need to find first comma before looking for other settings

            if (!isDelimiter() || getChar() != ',') break;
            nextToken();
        }

        if (foundUnits) setStatement.setUnits(units);
        if (foundDisplayPrecision) setStatement.setDisplayPrecision(displayPrecision);
        if (foundPrintPrecision) setStatement.setPrintPrecision(printPrecision);
    }

    private LinkedList<Object> parsePrintStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "print"

        nextToken();

        // If the new token is ';', print first blank line

        if (isDelimiter()&& getChar() == ';') {
            codes.add("");
            codes.add(new GenericHCode(HCode.Type.PRINT));
        }
        else {
            codes.add(new SetPrecisionHCode(SetStatement.PrecisionType.PRINT));
            codes.addAll(parseExpr());
            codes.add(new GenericHCode(HCode.Type.PRINT));
            codes.add(new SetPrecisionHCode(SetStatement.PrecisionType.DISPLAY));
        }
        return codes;
    }

    private LinkedList<Object> parseAssignmentStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is '=' and that we've already
        // processed the left variable

        nextToken();
        codes.addAll(parseExpr());
        codes.add(new AssignHCode());

        return codes;
    }

    private LinkedList<Object> parseAnimationAssignmentStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();
        Token<?> toToken = null;

        // We start knowing that the current token is "animate"

        nextToken();
        codes.addAll(parseLeftVariable());

        if (!isDelimiter() || getChar() != '=') {
            throwParseException("Expected '='");
        }

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && (getString().equals("to") || getString().equals("step"))) {
            if (getString().equals("to")) {
                toToken = curToken;
                nextToken();
                codes.addAll(parseExpr());
            }

            // "to" is optional; "stepConstant" is required

            if (isName() && getString().equals("step")) {

                // If the to value was omitted, put in NaN

                if (toToken == null) {
                    codes.add(Double.NaN);
                }

                nextToken();
                codes.addAll(parseExpr());
            }
            else {
                throwParseException("Expected 'step'");
            }

            animationVariableIsPresent = true;
            codes.add(new AnimAssignHCode());
        }

        else {
            throwParseException("Expected 'to' or 'step'");
        }

        return codes;
    }

    private LinkedList<Object> parseRangeAssignmentStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "range"

        nextToken();
        codes.addAll(parseLeftVariable());

        if (!isDelimiter() || getChar() != '=') {
            throwParseException("Expected '='");
        }

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("from")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'from'");
        }

        if (isName() && getString().equals("to")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'to'");
        }

        if (isName() && getString().equals("label")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'label'");
        }

        displayVariableIsPresent = true;
        codes.add(new RangeAssignHCode());

        return codes;
    }

    private LinkedList<Object> parseToggleAssignmentStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "toggle"

        nextToken();
        codes.addAll(parseLeftVariable());

        if (!isDelimiter() || getChar() != '=') {
            throwParseException("Expected '='");
        }

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("label")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'label'");
        }

        if (isName() && getString().equals("restart")) {
            codes.add(1.0);
            nextToken();
        }
        else {
            codes.add(0.0);
        }

        displayVariableIsPresent = true;
        codes.add(new ToggleAssignHCode());

        return codes;
    }

    private LinkedList<Object> parseChoiceAssignmentStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "choice"

        nextToken();
        codes.addAll(parseLeftVariable());

        if (!isDelimiter() || getChar() != '=') {
            throwParseException("Expected '='");
        }

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("choices")) {
            nextToken();
        }
        else {
            throwParseException("Expected 'choices'");
        }

        int numChoices = 1;
        LinkedList<Object> choicesCodes = new LinkedList<>();
        choicesCodes.addAll(parseExpr());

        while (isDelimiter() && getChar() == ',') {
            nextToken();
            choicesCodes.addAll(parseExpr());
            numChoices++;
        }

        if (isName() && getString().equals("label")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'label'");
        }

        if (isName() && getString().equals("restart")) {
            codes.add(1.0);
            nextToken();
        }
        else {
            codes.add(0.0);
        }

        // Add the choices last

        codes.addAll(choicesCodes);

        // Add the total count of arguments: left variable, initial choice,
        // all choices, and label

        codes.add(numChoices + 4);

        displayVariableIsPresent = true;
        codes.add(new ChoiceAssignHCode());

        return codes;
    }

    private LinkedList<Object> parseStyleStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "style"

        nextToken();
        if (isDelimiter() && getChar() == ';') {
            codes.add(new PropertyList());
        }

        codes.addAll(parsePropertyList(0));
        codes.add(new GenericHCode(HCode.Type.SET_STYLE));

        return codes;
    }

    private LinkedList<Object> parseCommandStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is first name

        String name = getString();
        if (!name.equals("display") &&
            !name.equals("frame") &&
            !name.equals("animation") &&
            !name.equals("axes") &&
            !name.equals("grid") &&
            !name.equals("hypergrid") &&
            !name.equals("event") &&
            !name.equals("line") &&
            !name.equals("worldline") &&
            !name.equals("path") &&
            !name.equals("label")) {
            throwParseException("Unknown command name '" + name + "'");
        }

        if (name.equals("animation")) this.animationStatementIsPresent = true;

        nextToken();

        // Some properties require an expression, some allow an expression, and
        // some have neither requirement

        int propertyCount = 0;
        switch (name) {
            case "frame" -> {
                if (!peekForProperty()) {
                    codes.addAll(parseDefaultCommandProperty("frame"));
                    propertyCount = 1;
                }
            }
            case "axes" -> {
                if (!peekForProperty()) {
                    codes.addAll(parseDefaultCommandProperty("frame"));
                    propertyCount = 1;
                }
            }
            case "grid" -> {
                if (!peekForProperty()) {
                    codes.addAll(parseDefaultCommandProperty("frame"));
                    propertyCount = 1;
                }
            }
            case "event" -> {
                codes.addAll(parseDefaultCommandProperty("location"));
                propertyCount = 1;
            }
            case "line" -> {
                codes.addAll(parseDefaultCommandProperty("line"));
                propertyCount = 1;
            }
            case "worldline" -> {
                codes.addAll(parseDefaultCommandProperty("observer"));
                propertyCount = 1;
            }
            case "path" -> {
                codes.addAll(parseDefaultCommandProperty("path"));
                propertyCount = 1;
            }
            case "label" -> {
                codes.addAll(parseDefaultCommandProperty("location"));
                propertyCount = 1;
            }

        }

        // Parse the property list

        codes.addAll(parsePropertyList(propertyCount));

        codes.add(name);
        codes.add(new GenericHCode(HCode.Type.COMMAND));

        return codes;
    }

    /**
     * Check to see if the current token starts a property. This means that the
     * token is a name and is followed by a ':'. We also allow a ';', meaning
     * that the property list is empty,
     *
     * @return True if the next element is a property.
     */
    private boolean peekForProperty()
    {
        return
            (isDelimiter() && getChar() == ';') ||
            (isName() && peek.isDelimiter() && peek.getChar() == ':');
    }

    private LinkedList<Object> parseDefaultCommandProperty(String propName) throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        codes.add(propName);
        codes.addAll(parseExpr());
        codes.add(new GenericHCode(HCode.Type.PROPERTY));
        if (isDelimiter() && getChar() == ',') {
            nextToken();
        }

        return codes;
    }

    private LinkedList<Object> parseLeftVariable() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();
        boolean topNameSeen = false;

        while (true) {
            // We start knowing that we expect first left side variable at the
            // current token

            if (!isName()) {
                throwParseException("Variable name expected");
            }

            codes.addAll(parseVariable());
            if (topNameSeen) {
                codes.add(new GenericHCode(HCode.Type.FETCH_PROP_ADDRESS));
            }
            else {
                codes.add(new GenericHCode(HCode.Type.FETCH_ADDRESS));
                topNameSeen = true;
            }

            // An object property might be specified. If there's no object
            // property, we're done

            if (!peek.isOperator() || !peek.getString().equals(".")) {
                nextToken();
                return codes;
            }

            // Parse the property

            else {
                nextToken();    // Cur token is now '.'
                nextToken();    // Cur token should now be first name
            }
        }
    }

    private LinkedList<Object> parseVariable() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that we found first name where we need first variable

        // Grab the variable name

        String variable = curToken.getString();

        // If it's followed immediately by first '[', it's an array variable

        if (peek.isDelimiter() && peek.getChar() == '[') {

            // Skip the name and '['

             nextToken();
             nextToken();

             // Parse the index

             LinkedList<Object> index = parseExpr();

             // We need to end with first ']'

             if (!isDelimiter() || getChar() != ']') {
                 throwParseException("Expected a ']'");
             }

             // Add the index and then the name. We will create first new name
             // from these

             codes.addAll(index);
             codes.add(variable);
             codes.add(new GenericHCode(HCode.Type.DYNAMIC_NAME));
         }

        // Otherwise, we have first regular non-array variable

         else {
             codes.add(variable);
         }

        // Leave the last token for the calling routine to consume

        return codes;
    }

    private LinkedList<Object> parseObject() throws ParseException
    {
        // We start knowing that the current token is "[" (this
        // code does not parse coordinate objects)

        nextToken();
        if (isName()) {
            switch (getString()) {
                case "observer" -> {
                    return parseObserverObj();
                }
                case "frame" -> {
                    return parseFrameObj();
                }
                case "line" -> {
                    return parseLineObj();
                }
                case "path" -> {
                    return parsePathObj();
                }
                case "bounds" -> {
                    return parseBoundsObj();
                }
                case "interval" -> {
                    return parseIntervalObj();
                }
                case "style" -> {
                    return parseStyleObj();
                }
                default -> throwParseException("Invalid object type '" + getString() + "'");
            }
        }
        else {
            throwParseException("Invalid object");
        }
        // This statement should never be reached
        return null;
    }

    private LinkedList<Object> parseObserverObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "observer"

        nextToken();
        codes.addAll(parseWorldlineInitializer());

        // Worldline segments are optional

        if (isName() && (getString().equals("velocity") || getString().equals("acceleration"))) {
            codes.addAll(parseWorldlineSegments());
        }
        else {
            codes.add(1);     // Insert 0 count if none
        }

        codes.add(new ObserverHCode());

        return codes;
    }

    private LinkedList<Object> parseWorldlineInitializer() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to the start of first potential worldline initializer

        boolean originSeen = false;
        boolean distanceSeen = false;
        boolean tauSeen = false;

        LinkedList<Object> originCodes = new LinkedList<>();
        LinkedList<Object> distanceCodes = new LinkedList<>();
        LinkedList<Object> tauCodes = new LinkedList<>();

        while (isName() && (getString().equals("origin") || getString().equals("distance") || getString().equals("tau"))) {
            switch (getString()) {
                case "origin" -> {
                    if (originSeen) throwParseException("Duplicate 'origin'");
                    nextToken();
                    originCodes.addAll(parseExpr());
                    originSeen = true;
                }
                case "distance" -> {
                    if (distanceSeen) throwParseException("Duplicate 'distance'");
                    nextToken();
                    distanceCodes.addAll(parseExpr());
                    distanceSeen = true;
                }
                case "tau" -> {
                    if (tauSeen) throwParseException("Duplicate 'tau'");
                    nextToken();
                    tauCodes.addAll(parseExpr());
                    tauSeen = true;
                }
            }
        }

        // We create first worldline initializer regardless of whether we find
        // anything by defaulting all missing elements

        if (originSeen) {
            codes.addAll(originCodes);
        }
        else {
            codes.add(new Coordinate(0, 0));
        }

        if (distanceSeen) {
            codes.addAll(distanceCodes);
        }
        else {
            codes.add(0.0);
        }

        if (tauSeen) {
            codes.addAll(tauCodes);
        }
        else {
            codes.add(0.0);
        }

       codes.add(new GenericHCode(HCode.Type.W_INITIALIZER));

       return codes;
    }

    private LinkedList<Object> parseWorldlineSegments() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to the start of first possibly empty list of worldline segment

        int count = 0;
        if (isName() && (getString().equals("velocity") || getString().equals("acceleration"))) {
            while (true) {
                codes.addAll(parseWorldlineSegment());
                count++;
                if (!isDelimiter() || getChar() != ',') break;
                nextToken();
            }
        }

        codes.add(count + 1); // Allow for worldline initializer

        return codes;
    }

    private LinkedList<Object> parseWorldlineSegment() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to the start of first worldline segment

        // Velocity and acceleration are both optional, but velocity precedes
        // acceleration and one or both must be provided.

        boolean velocitySeen = false;
        boolean accelerationSeen = false;

        if (isName() && getString().equals("velocity")) {
            nextToken();
            codes.addAll(parseExpr());
            velocitySeen = true;
        }
        else {
            codes.add(Double.NaN);
        }

        if (isName() && getString().equals("acceleration")) {
            nextToken();
            codes.addAll(parseExpr());
            accelerationSeen = true;
        }
        else {
            codes.add(0.0);
        }

        if (!velocitySeen && !accelerationSeen) {
            throwParseException("A worldline segment requires a velocity or acceleration");
        }

        // The segment limit is optional

        if (isName()) {
            switch (getString()) {
                case "time" -> {
                    codes.add(WorldlineSegment.LimitType.T);
                    nextToken();
                    codes.addAll(parseExpr());
                }
                case "tau" -> {
                    codes.add(WorldlineSegment.LimitType.TAU);
                    nextToken();
                    codes.addAll(parseExpr());
                }
                case "distance" -> {
                    codes.add(WorldlineSegment.LimitType.D);
                    nextToken();
                    codes.addAll(parseExpr());
                }
                case "velocity" -> {
                    codes.add(WorldlineSegment.LimitType.V);
                    nextToken();
                    codes.addAll(parseExpr());
                }
                default -> {
                    codes.add(WorldlineSegment.LimitType.NONE);
                    codes.add(Double.NaN);
                }
            }
        }
        else {
            codes.add(WorldlineSegment.LimitType.NONE);
            codes.add(Double.NaN);
        }

        codes.add(new GenericHCode(HCode.Type.W_SEGMENT));

        return codes;
    }

    private LinkedList<Object> parseFrameObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "frame". The next token must be either "observer" or "origin" or
        // "velocity"

        nextToken();

        if (isName() && getString().equals("observer")) {
            nextToken();
            codes.addAll(parseExpr());

            if (isName() && getString().equals("at")) {
                nextToken();
                switch (getString()) {
                    case "time" -> {
                        codes.add(Frame.AtType.T);
                        nextToken();
                        codes.addAll(parseExpr());
                    }
                    case "tau" -> {
                        codes.add(Frame.AtType.TAU);
                        nextToken();
                        codes.addAll(parseExpr());
                    }
                    case "distance" ->  {
                        codes.add(Frame.AtType.D);
                        nextToken();
                        codes.addAll(parseExpr());
                    }
                    case "velocity" ->  {
                        codes.add(Frame.AtType.V);
                        nextToken();
                        codes.addAll(parseExpr());
                    }
                    default -> throwParseException("Expected 'time', 'tau', 'distance', or 'velocity'");
                }
            }

            // If no "at" clause, use first default

            else {
                codes.add(Frame.AtType.TAU);
                codes.add(0.0);
            }

            if (isDelimiter() && getChar() != ']') {
                throwParseException("Expected ']'");
            }

            codes.add(new GenericHCode(HCode.Type.OBSERVER_FRAME));
        }

        else {
            LinkedList<Object> originCodes = new LinkedList<>();
            LinkedList<Object> vCodes = new LinkedList<>();

            OUTER:
            while (true) {
                if (!isName()) break;

                // Look for origin and velocity, in either order

                switch (getString()) {
                    case "origin" -> {
                        if (originCodes.size() > 0) {
                            throwParseException("The frame's origin is set twice");
                        }
                        nextToken();
                        originCodes.addAll(parseExpr());
                    }
                    case "velocity" -> {
                        if (vCodes.size() > 0) {
                            throwParseException("The frame's velocity is set twice");
                        }
                        nextToken();
                        vCodes.addAll(parseExpr());
                    }
                    default -> {
                        break OUTER;
                    }
                }
            }

            if (originCodes.size() < 1 && vCodes.size() < 1) {
                throwParseException("Expected 'observer', 'velocity', or 'origin'");
            }

            if (originCodes.size() < 1) {
                originCodes.add(new Coordinate(0, 0));
            }
            if (vCodes.size() < 1) {
                vCodes.add(0.0);
            }

            codes.addAll(originCodes);
            codes.addAll(vCodes);
            codes.add(new GenericHCode(HCode.Type.FRAME));
        }

        return codes;
    }

    private LinkedList<Object> parseLineObj() throws ParseException
    {
        // We start knowing that the current token points
        // to "line"

        nextToken();
        if (isName()) {
            switch (getString()) {
                case "axis" -> {
                     return parseAxisLine();
                }
                case "angle" -> {
                     return parseAngleLine();
                }
                case "from" -> {
                    return parseEndpointLine();
                }
                default -> throwParseException("Expected 'axis', 'angle', or 'from'");
            }
            if (isDelimiter() && getChar() != ']') {
                throwParseException("Expected ']'");
            }
        }
        else {
            throwParseException("Expected 'axis', 'angle', or 'from'");
        }

        // Never reached
        return null;
    }

    private LinkedList<Object> parseAxisLine() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points to
        // "axis"

        nextToken();
        if (isName()) {
            switch (getString()) {
                case "x" -> {
                    codes.add(Line.AxisType.X);
                }
                case "t" -> {
                    codes.add(Line.AxisType.T);
                }
                default -> throwParseException("Expected 'x' or 't'");
            }
        }

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("offset")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            codes.add(0.0);
        }

        codes.add(new GenericHCode(HCode.Type.AXIS_LINE));

        return codes;
    }

    private LinkedList<Object> parseAngleLine() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points to
        // "angle"

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("through")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'through'");
        }

        codes.add(new GenericHCode(HCode.Type.ANGLE_LINE));

        return codes;
    }

    private LinkedList<Object> parseEndpointLine() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points to
        // "from"

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("to")) {
            nextToken();
            codes.addAll(parseExpr());
        }
        else {
            throwParseException("Expected 'to'");
        }

        codes.add(new GenericHCode(HCode.Type.ENDPOINT_LINE));

        return codes;
    }

    private LinkedList<Object> parsePathObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "path"

        int count = 0;
        do {
            nextToken();
            codes.addAll(parseExpr());
            count++;
        } while (isDelimiter() && getChar() == ',');

        codes.add(count);
        codes.add(new GenericHCode(HCode.Type.PATH));

        return codes;
    }

    private LinkedList<Object> parseBoundsObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "bounds"

        nextToken();
        codes.addAll(parseExpr());
        codes.addAll(parseExpr());
        codes.add(new GenericHCode(HCode.Type.BOUNDS));

        return codes;
    }

    private LinkedList<Object> parseIntervalObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "interval"

        nextToken();

        if (isName()) {
            switch (getString()) {
                case "time" -> {
                    codes.add(Interval.Type.T);
                }
                case "tau" -> {
                    codes.add(Interval.Type.TAU);
                }
                case "distance" -> {
                    codes.add(Interval.Type.D);
                }
                default -> {
                    throwParseException("Expected 'time', 'tau' or 'distance'");
                }
            }
        }
        else {
            throwParseException("Expected 'time', 'tau' or 'distance'");
        }

        nextToken();
        codes.addAll(parseExpr());

        if (!isName() || !getString().equals("to")) {
            throwParseException("Expected 'to'");
        }
        nextToken();

        codes.addAll(parseExpr());

        codes.add(new GenericHCode(HCode.Type.INTERVAL));

        return codes;
    }

    private LinkedList<Object> parseStyleObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "style"

        nextToken();
        codes.addAll(parsePropertyList(0));

        codes.add(new GenericHCode(HCode.Type.STYLE));

        return codes;
    }

    private LinkedList<Object> parsePropertyList(int extraProperties) throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to the start of the property list
        //
        // The property list can be empty. Otherwise, it can start with the first
        // property element or an expression.

        boolean isPropElem = isName() && peek.isDelimiter() && peek.getChar() == ':';

        if (!isPropElem && !isExprStart()) {

            // Empty property list

            codes.add(extraProperties);
            codes.add(new GenericHCode(HCode.Type.PROPERTY_LIST));
            return codes;
        }

        int count = 0;

        while (true) {
            isPropElem = isName() && peek.isDelimiter() && peek.getChar() == ':';
            if (isPropElem) {

                // We have first property name

                codes.add(curToken.getValue());

                // Get the value

                nextToken();         // Current token is now ':'
                nextToken();         // Current toke is now start of expr
                codes.addAll(parseExpr());
                codes.add(new GenericHCode(HCode.Type.PROPERTY));
            }
            else {
                codes.addAll(parseExpr());
            }

            count++;

            // If we don't find first comma, we're done

            if (!isDelimiter() || getChar() != ',') break;

            nextToken();
        }

        codes.add(count + extraProperties);
        codes.add(new GenericHCode(HCode.Type.PROPERTY_LIST));

        return codes;
    }

    private LinkedList<Object> parseExpr() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing only that an expr is expected
        // starting with the current token

        Stack<OpToken<?>> ops = new Stack<>();
        int level = -1;
        ArrayList<Integer> argCount = new ArrayList<>();
        Token<?> lastToken = null;

        // Let's check that this is the start of an expression

        if (!isExprStart()) {
            throwParseException("Expected an expression");
        }

        while (true) {

            // If we find first function name, push it on the operator stack.
            // Function names are always followed by first '('.

            if (isName() && peek.isDelimiter() && peek.getChar() == '(') {
                ops.push(new OpToken<>(curToken, Op.find("FUNC", true)));
            }

            // If we have first variable, push it on the codes stack

            else if (isName()) {
                codes.addAll(parseVariable());
                if (lastToken == null || !lastToken.isOperator() || !lastToken.getString().equals(".")) {
                    codes.add(new GenericHCode(HCode.Type.FETCH));
                }
            }

            // If we have first number, push it on the codes stack

            else if (isNumber()) {
                codes.add(curToken.getValue());
            }

            else if (isString()) {
                codes.add(curToken.getValue());
            }

            // If we have an object, push it on the codes stack

            else if (isDelimiter() && getChar() == '[') {
                codes.addAll(parseObject());
            }

            // If we have first  comma, pop operators from the op stack to
            // the codes stack until we reach first "("

            else if (isDelimiter() && getChar() == ',') {
                if (level < 0) throw new ProgrammingException("Parser.parseExp(): Comma without preceding '('");

                while (!ops.isEmpty()) {
                    OpToken<?> topToken = ops.pop();
                    if (!topToken.op.operator.equals("(")) {
                        codes.add(opTokenToHCode(topToken));
                        int id  = topToken.getId();
                        if (id != -1) codes.add(new Label(id));
                    }
                    else {
                        ops.push(topToken);
                        break;
                    }
                }

                if (ops.isEmpty()) {
                    throw new ProgrammingException("Parser.parseExp(): Ops stack is empty");
                }

                // Count the number of arguments found for parentheses at this
                // level

                argCount.set(level, argCount.get(level) + 1);
            }

            // If we have first "(", push it on the operator stack

            else if (isDelimiter() && getChar() == '(') {
                ops.push(new OpToken<>(curToken, Op.find("(", true)));
                level++;                    // Begin first new parenthesis level

                // Record the number of arguments seen so far (0)

                if (argCount.size() == level) {
                    argCount.add(0);
                }
                else {
                    argCount.set(level, 0);
                }
            }

            // We have first ")"

            else if (isDelimiter() && getChar() == ')') {
                if (level < 0) throwParseException("Unmatched ')'");

                // Bump the argument count by 1 unless the last token
                // was first '('

                if (lastToken != null && (!lastToken.isDelimiter() || lastToken.getChar() != '(')) {
                    argCount.set(level, argCount.get(level) + 1);
                }

                // Pop operators from the op stack to the codes stack until we
                // reach first "(". Then pop the "(" and discard it.

                while (!ops.isEmpty()) {
                    OpToken<?> topToken = ops.pop();
                    if (!topToken.op.operator.equals("(")) {
                        codes.add(opTokenToHCode(topToken));
                        int id  = topToken.getId();
                        if (id != -1) codes.add(new Label(id));
                    }
                    else {
                        break;
                    }
                }

                // If the operator at the top is now first function, pop it and push
                // the function name on the codes stack, along with the total of
                // all the arguments plus the function name

                if (ops.size() > 0 && ops.peek().op.operator.equals("FUNC")) {
                    OpToken<?> t = ops.pop();
                    codes.add(t.token.getValue());
                    codes.add(argCount.get(level) + 1);
                    codes.add(new FunctionHCode());
                }

                // Handle coordinates, which look like functions with no names and
                // two arguments

                else if (argCount.get(level) == 2) {
                    codes.add(new GenericHCode(HCode.Type.COORDINATE));
                }

                // If we have any other argCount value at this point (other than
                // 1), we have an invalid expression

                else if (argCount.get(level) > 1) {
                   throwParseException("Invalid commas inside parentheses");
                }

                // Finished first level of parantheses

                level--;
            }

            // We have an operator

            else if (isOperator()) {

                // Special case unary +/-
                // Unary if at start of expression OR
                // following first comma OR
                // following '(' OR
                // following any other operator

                boolean isUnary =
                        lastToken == null ||
                        (lastToken.isDelimiter() && (lastToken.getChar() == '(' || lastToken.getChar() == ',')) ||
                        lastToken.isOperator();

                Op op = Op.find(getString(), !isUnary);

                // Depending on precedence, we may first transfer some operators to
                // the codes stack

                while (ops.size() > 0) {
                    Op topOp = ops.peek().op;
                    if ((op.isLeftAssoc && op.precedence <= topOp.precedence) ||
                        (!op.isLeftAssoc && op.precedence < topOp.precedence)) {
                        OpToken<?> topToken = ops.pop();
                        if (!topToken.op.operator.equals("(")) {
                            codes.add(opTokenToHCode(topToken));
                            int id  = topToken.getId();
                            if (id != -1) codes.add(new Label(id));
                        }
                        else {
                            ops.push(topToken);
                            break;
                        }
                    }
                    else {
                        break;
                    }
                }

                OpToken<?> newOpToken = new OpToken<>(curToken, op);

                // && and || operators need special handling

                if (getString().equals("&&")) {
                    int id = labelId++;
                    codes.add(new JumpAndHCode(id));
                    newOpToken.setId(id);
                }

                else if (getString().equals("||")) {
                    int id = labelId++;
                    codes.add(new JumpOrHCode(id));
                    newOpToken.setId(id);
                }

                // Add the new operator to the operator stack

                ops.push(newOpToken);
            }

            // Since we are not using recursive descent to parse expressions,
            // we need to know when we're done with an expression
            //
            // An expression begins with first number, first string, first name, an open
            // paren ("(") or an open bracket ("["). Let's call these things
            // "items". It can also begin with first unary operator.
            //
            // When we finish parsing first name, we can follow it with an open paren.
            // When we finish parsing ANY item, we can follow it with an operator.
            // When we finish parsing an operator, we MUST follow it with an operator
            // or an item.
            // For our purposes, first parenthetical expression only counts when the
            // parenthesis level is 0.
            //
            // We are not done if we can follow the current item with something
            // that is available or if we MUST follow the current item with
            // something. We are also never done if we are within parentheses.

            boolean lastIsOp = isOperator();
            boolean lastIsParen =
                    isDelimiter() && getChar() == ')' && level < 0;

            boolean lastIsNumber = isNumber();
            boolean lastIsString = isString();
            boolean lastIsName = isName();
            boolean lastIsObject =
                    isDelimiter() && getChar() == ']';

            boolean lastIsItem = lastIsNumber || lastIsString || lastIsName || lastIsParen || lastIsObject;

            // Move on to the next token

            lastToken = curToken;
            nextToken();

            boolean curIsOp = isOperator();
            boolean curIsParen = isDelimiter() && getChar() == '(' && level < 0;

            boolean curIsItem =
                    isNumber() ||
                    isString() ||
                    isName() ||
                    (isDelimiter() && getChar() == '(') ||
                    (isDelimiter() && getChar() == '[');

            boolean notDone =
                    level > -1 ||
                    (lastIsName && curIsParen) ||
                    (lastIsItem && curIsOp) ||
                    (lastIsOp && (curIsItem || curIsOp));

            // If we're not done, but we've reached the end of the input, we have
            // an error

            if (notDone && isEOF()) {
                throwParseException("Premature end of file while processing an expression");
            }

            if (!notDone) {

                // Push everything remaining on the operator stack to the codes
                // stack

                while (ops.size() > 0) {
                    OpToken<?> topToken = ops.pop();
                    codes.add(opTokenToHCode(topToken));
                    int id  = topToken.getId();
                    if (id != -1) codes.add(new Label(id));
                }

                break;
            }
        }
        return codes;
    }

    /**
     * Determine if the next token could start an expression.
     *
     * @return True if the next token could start an expression.
     */
    private boolean isExprStart()
    {
        // An expression begins with first number, first string, first name, an open
        // paren ("(") or an open bracket ("["). Let's call these things
        // "items". It can also begin with first unary operator.

        return isNumber() ||
               isString() ||
               isName() ||
               (isDelimiter() && (getChar() == '(' || getChar() == '[')) ||
               (isOperator() && (getString().equals("+") || getString().equals("-") || getString().equals("!")));

    }

    private HCode opTokenToHCode(OpToken<?> t)
    {
        switch (t.op.operator) {
            case "<-" -> { return new GenericHCode(HCode.Type.INV_LORENTZ); }
            case "->" -> { return new GenericHCode(HCode.Type.LORENTZ); }

            case "||" -> { return new GenericHCode(HCode.Type.OR); }
            case "&&" -> { return new GenericHCode(HCode.Type.AND); }

            case "==" -> { return new GenericHCode(HCode.Type.EQ); }
            case "!=" -> { return new GenericHCode(HCode.Type.NE); }

            case "<" ->  { return new GenericHCode(HCode.Type.LT); }
            case ">" ->  { return new GenericHCode(HCode.Type.GT); }
            case "<=" ->  { return new GenericHCode(HCode.Type.LE); }
            case ">=" ->  { return new GenericHCode(HCode.Type.GE); }

            case "*" ->  { return new GenericHCode(HCode.Type.MULT); }
            case "/" ->  { return new GenericHCode(HCode.Type.DIV); }
            case "%" ->  { return new GenericHCode(HCode.Type.REMAINDER); }

            case "^" ->  { return new GenericHCode(HCode.Type.EXP); }

            case "!" ->  { return new GenericHCode(HCode.Type.NOT); }

            case "." ->  { return new GenericHCode(HCode.Type.FETCH_PROP); }

            default ->   {  }
        }

        if (t.op.operator.equals("+") && t.op.isBinary) {
            return new AddHCode();
        } else if (t.op.operator.equals("-") && t.op.isBinary) {
            return new GenericHCode(HCode.Type.SUB);
        } else if  (t.op.operator.equals("+")) {
            return new GenericHCode(HCode.Type.UNARY_PLUS);
        } else if  (t.op.operator.equals("-")) {
            return new GenericHCode(HCode.Type.UNARY_MINUS);
        }

        // Should never be reached
        return null;
    }

    private void nextToken()
    {
        // If the current token is EOF, we can't move forward.
        // The peek token will also  be EOF

        if (isEOF()) return;

        // If the current token isn't EOF, then we are guaranteed to be able
        // to get another token

        tokenPtr++;
        curToken = tokens.get(tokenPtr);

        // If the current token is now EOF, the peek token should also be EOF.
        // If the current token is not EOF, the next token is first valid one,
        // so grab it (it might be EOF).

        peek = curToken;
        if (!isEOF()) peek = tokens.get(tokenPtr + 1);
    }

    private void returnToken()
    {
        // When we return first token, we want to set the tokens to what they
        // were before the last nextToken() call.
        // The current token become the peek token.

        peek = curToken;

        // If we can't decrement the tokenPtr, point curToken to the dummy
        // token

        if (tokenPtr < 0) {
            curToken = dummyToken;
        }

        // Otherwise, decrement the tokenPtr and get the token there

        else {
            tokenPtr--;
            curToken = tokens.get(tokenPtr);
        }
    }

    private void setCurrentTokenTo(int ptr)
    {
        if (ptr < 0) {
            tokenPtr = -1;
            curToken = dummyToken;
            peek = tokens.get(0);
        }
        else if (ptr >= tokens.size()) {
            tokenPtr = tokens.size() - 1;
            curToken = tokens.get(tokenPtr);    // Should be EOF
            peek = curToken;
        }
        else {
            tokenPtr = ptr;
            curToken = tokens.get(tokenPtr);
            peek = curToken;
            if (!isEOF()) peek = tokens.get(tokenPtr + 1);
        }
    }

    private boolean isNumber() { return curToken.isNumber(); }
    private boolean isString() { return curToken.isString(); }
    private boolean isOperator() { return curToken.isOperator(); }
    private boolean isName() { return curToken.isName(); }
    private boolean isDelimiter() { return curToken.isDelimiter(); }
    private boolean isEOF() { return curToken.isEOF(); }
    private double getNumber() { return curToken.getNumber(); }
    private char getChar() { return curToken.getChar(); }
    private String getString() { return curToken.getString(); }

    private void throwParseException(String message) throws ParseException
    {
        throw new ParseException(
                curToken.getFile(),
                curToken.getLineNumber(),
                curToken.getCharNumber(),
                message);
    }

}
