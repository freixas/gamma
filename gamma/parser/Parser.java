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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gamma.parser;

import gamma.ProgrammingException;
import gamma.execution.hcode.*;
import gamma.value.Coordinate;
import gamma.value.Frame;
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

        final char chr;
        final boolean isLeftAssoc;
        final boolean isBinary;
        final int precedence;

        static HashMap<Character, Op> binary = new HashMap<>();
        static HashMap<Character, Op> unary = new HashMap<>();

        @SuppressWarnings("LeakingThisInConstructor")
        Op(char chr, boolean isLeftAssoc, boolean isBinary, int precedence)
        {
            this.chr = chr;
            this.isLeftAssoc = isLeftAssoc;
            this.isBinary = isBinary;
            this.precedence = precedence;

            // Store each operator in a table so that we can re-use the
            // instances

            if (isBinary) {
                binary.put(chr, this);
            }
            else {
                unary.put(chr, this);
            }

            initialized = true;
        }

        /**
         * A factory method for creating operators. Given a character,
         * it finds an returns and matching operator.
         *
         * @param chr
         * @return
         */
        static Op find(char chr, boolean isBinary)
        {
            Op result;
            if (isBinary) {
                result = binary.get(chr);
            }
            else {
                result = unary.get(chr);
            }
            if (result == null) {
                throw new RuntimeException("Op.find failed to find chr '" + chr + "' in " + (isBinary ? "binary" : "unary") + " table");
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
            return "Op{" + "chr=" + chr + '}';
        }
    }

    class OpToken<T> extends Token<T>
    {
        private final Token token;
        private final Op op;

        OpToken(Token<T> token, Op op)
        {
            super(token.getType(), token.getValue(), token.getFile(), token.getLineNumber(), token.getCharNumber());
            this.token = token;
            this.op = op;
        }

        @Override
        public String toString()
        {
            return "OpToken{" + "token=" + token + ", op=" + op + '}';
        }

    }

    private final File file;
    private final String script;
    private ArrayList<Token> tokens;
    private LinkedList<Object> hCodes;

    private boolean animationStatementIsPresent = false;
    private boolean animationVariableIsPresent = false;

    private SetStatement setStatement;

    private final Token dummyToken = new Token<>(Token.Type.DELIMITER, '~', null, 0, 0);

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
            new Op('^', false, true, 15);
            new Op('*', true, true, 14);
            new Op('/', true, true, 14);
            new Op('+', true, true, 13);
            new Op('-', true, true, 13);
            new Op('<', true, true, 12);
            new Op('>', true, true, 12);
            new Op('+', false, false, 16);
            new Op('-', false, false, 16);
            new Op('.', true, true, 20);
            new Op('(', false, true, 21);
            new Op('!', true, true, 1000); // Used for function names
        }
    }

    /**
     * Get the tokens produced by parsing.
     *
     * @return The tokens produced by parsing.
     */
    public ArrayList<Token> getTokens()
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
        setStatement = new SetStatement();

        Tokenizer tokenizer = new Tokenizer(file, script);
        tokens = tokenizer.tokenize();

        // tokenPtr points to the current token.
        // When we start, it points one before the start of the token list.
        // The current token is a dummy token.
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

            // A statement can be empty

            if (isName()) {
                codes.add(new LineInfoHCode(curToken.getFile(), curToken.getLineNumber()));
                switch (getString()) {
                    case "include" -> parseIncludeStatement();
                    case "stylesheet" -> parseStylesheetStatement();
                    case "set" -> parseSetStatement();
                    case "print" -> codes.addAll(parsePrintStatement());
                    case "let" -> codes.addAll(parseAssignmentStatement());
                    case "style" -> codes.addAll(parseStyleStatement());
                    default -> codes.addAll(parseCommandStatement());
                }
            }

            // Deliberately fall through

            if (!isDelimiter() || getChar() != ';') {
                throwParseException("Expected the start of a statement");
            }

            nextToken();
        }

        return codes;
    }

    private void parseIncludeStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "include"

        nextToken();

        // The next token has to be a string. We don't have the ability to
        // process expressions in the parser

        if (!isString()) {
            throwParseException("Missing include file name");
        }

        // If the next token isn't a ';', return an empty codes list

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
            ArrayList<Token> includeTokens = tokenizer.tokenize();

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
         LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "stylesheet"

        nextToken();

        return null;
    }

    private void parseSetStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "set"

        nextToken();

        boolean foundUnits = false;
        boolean foundDisplayPrecision = false;
        boolean foundPrintPrecision = false;

        double units = SetStatement.DEFAULT_UNITS;
        double displayPrecision = SetStatement.DEFAULT_DISPLAY_PRECISION;
        double printPrecision = SetStatement.DEFAULT_PRINT_PRECISION;

        while (true) {

            // We expect a name; if we don't get one, we're done

            if (!isName()) break;

            // Look for units

            if (getString().equals("units")) {
                if (foundUnits) {
                    throwParseException("Units are set twice");
                }

                nextToken();
                if (!isNumber()) {
                    throwParseException("Units must be set to a floating point number >= 0");
                }
                units = getNumber();
                nextToken();
                foundUnits = true;
            }

            // Look for displayPrecision

            else if (getString().equals("displayPrecision")) {
                if (foundDisplayPrecision) {
                    throwParseException("Display precision is set twice");
                }

               nextToken();
                if (!isNumber()) {
                    throwParseException("Display precision must be set to a floating point number >= 0");
                }
                displayPrecision = getNumber();
                nextToken();
                foundDisplayPrecision = true;
            }

            // Look for printPrecision

            else if (getString().equals("printPrecision")) {
                if (foundPrintPrecision) {
                    throwParseException("Print precision is set twice");
                }

               nextToken();
                if (!isNumber()) {
                    throwParseException("Print precision must be set to a floating point number >= 0");
                }
                printPrecision = getNumber();
                nextToken();
                foundPrintPrecision = true;
            }

            // We're also done if we get any other name

            else {
                break;
            }
        }

        setStatement.set(units, displayPrecision, printPrecision);
    }

    private LinkedList<Object> parsePrintStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is "let"

        nextToken();

        // If the new token is ';', print a blank line

        if (isDelimiter()&& getChar() == ';') {
            codes.add("");
            codes.add(new PrintHCode());
        }
        else {
            codes.add(new SetPrecisionHCode(SetStatement.PrecisionType.PRINT));
            codes.addAll(parseExpr());
            codes.add(new PrintHCode());
            codes.add(new SetPrecisionHCode(SetStatement.PrecisionType.DISPLAY));
        }
        return codes;
    }

    private LinkedList<Object> parseAssignmentStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();
        Token toToken = null;
        Token stepToken = null;

        // We start knowing that the current token is "let"

        nextToken();
        codes.addAll(parseLeftVariable());

        if (!isDelimiter() || getChar() != '=') {
            throwParseException("Expected '='");
        }

        nextToken();
        codes.addAll(parseExpr());

        if (isName() && getString().equals("to")) {
            toToken = curToken;
            nextToken();
            codes.addAll(parseExpr());
        }

        if (isName() && getString().equals("step")) {

            // If the to value was omitted, put in NaN

            if (toToken == null) {
                codes.add(Double.NaN);
            }

            stepToken = curToken;
            nextToken();
            codes.addAll(parseExpr());
        }

        // Can't have 'to' without 'step'

        if (toToken != null && stepToken == null) {
            throwParseException("Missing 'step' in animation assignment");
        }

        if (stepToken != null) {
            animationVariableIsPresent = true;
            codes.add(new AnimAssignHCode());
        }
        else {
            codes.add(new AssignHCode());
        }

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

        codes.addAll(parsePropertyList());
        codes.add(new SetStyleHCode());

        return codes;
    }

    private LinkedList<Object> parseCommandStatement() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token is a name

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
        Token nameToken = curToken;

        if (name.equals("animation")) this.animationStatementIsPresent = true;

        nextToken();

        // The property list could be empty

        if (isDelimiter() && getChar() == ';') {
            codes.add(new PropertyList());
        }
        else {
            codes.addAll(parsePropertyList());
        }

        codes.add(nameToken.getValue());
        codes.add(new CommandHCode());

        return codes;
    }

    private LinkedList<Object> parseLeftVariable() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();
        boolean topNameSeen = false;

        while (true) {
            // We start knowing that we expect a left side variable at the
            // current token

            if (!isName()) {
                throwParseException("Variable name expected");
            }

            codes.add(curToken.getValue());
            if (topNameSeen) {
                codes.add(new FetchPropAddressHCode());
            }
            else {
                codes.add(new FetchAddressHCode());
                topNameSeen = true;
            }

            // An object property might be specified. If there's no object
            // property, we're done

            if (!peek.isOperator() || peek.getChar() != '.') {
                nextToken();
                return codes;
            }

            // Parse the property
            else {
                nextToken();    // Cur token is now '.'
                nextToken();    // Cur token should now be a name
            }
        }
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
        // to the start of a potential worldline initializer

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

        // We create a worldline initializer regardless of whether we find
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

       codes.add(new WInitializerHCode());

       return codes;
    }

    private LinkedList<Object> parseWorldlineSegments() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to the start of a possibly empty list of worldline segment

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
        // to the start of a worldline segment

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

        if (isName() && (getString().equals("t") || getString().equals("tau") || getString().equals("distance"))) {
            switch (getString()) {
                case "t" -> {
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
                default -> throwParseException("Programming error");
            }
        }
        else {
            codes.add(WorldlineSegment.LimitType.NONE);
            codes.add(Double.NaN);
        }

        codes.add(new WSegmentHCode());

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

            // If no "at" clause, use a default

            else {
                codes.add(Frame.AtType.TAU);
                codes.add(0.0);
            }

            if (isDelimiter() && getChar() != ']') {
                throwParseException("Expected ']'");
            }

            codes.add(new ObserverFrameHCode());
        }

        else {
            LinkedList<Object> originCodes = new LinkedList<>();
            LinkedList<Object> vCodes = new LinkedList<>();

            while (true) {

                // We expect a name; if we don't get one, we're done

                if (!isName()) break;

                // Look for origin

                if (getString().equals("origin")) {
                    if (originCodes.size() > 0) {
                        throwParseException("The frame's origin is set twice");
                    }
                    nextToken();
                    originCodes.addAll(parseExpr());
                }

                // Look for velocity

                else if (getString().equals("velocity")) {
                    if (vCodes.size() > 0) {
                        throwParseException("The frame's velocity is set twice");
                    }
                    nextToken();
                    vCodes.addAll(parseExpr());
                }

                // We're also done if we get any other name

                else {
                    break;
                }
            }

            if (originCodes.size() < 1) {
                originCodes.add(new Coordinate(0, 0));
            }
            if (vCodes.size() < 1) {
                vCodes.add(0.0);
            }

            codes.addAll(originCodes);
            codes.addAll(vCodes);
            codes.add(new FrameHCode());
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

        codes.add(new AxisLineHCode());

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

        codes.add(new AngleLineHCode());

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

        codes.add(new EndpointLineHCode());

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
        codes.add(new PathHCode());

        return codes;
    }

    private LinkedList<Object> parseIntervalObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "interval"

        nextToken();

        LinkedList<Object> xCodes = new LinkedList<>();
        LinkedList<Object> tCodes = new LinkedList<>();

        while (true) {

            // We expect a name; if we don't get one, we're done

            if (!isName()) break;

            // Look for origin

            if (getString().equals("x")) {
                nextToken();
                if (xCodes.size() > 0) {
                    throwParseException("The interval already contains an x range");
                }
                xCodes.addAll(parseExpr());

                if (!(isName() && getString().equals("to"))) {
                    throwParseException("Expected 'to' for the interval's x range");
                }
                nextToken();
                xCodes.addAll(parseExpr());
            }

            // Look for velocity

            else if (getString().equals("t")) {
                nextToken();
                if (tCodes.size() > 0) {
                    throwParseException("The interval already contains a t range");
                }
                tCodes.addAll(parseExpr());
                if (!(isName() && getString().equals("to"))) {
                    throwParseException("Expected 'to' for the interval's t range");
                }
                nextToken();
                tCodes.addAll(parseExpr());
            }

            // We're also done if we get any other name

            else {
                break;
            }
        }

        if (xCodes.size() < 1) {
            xCodes.add(Double.NEGATIVE_INFINITY);
            xCodes.add(Double.POSITIVE_INFINITY);
        }
        if (tCodes.size() < 1) {
            tCodes.add(Double.NEGATIVE_INFINITY);
            tCodes.add(Double.POSITIVE_INFINITY);
        }

        codes.addAll(xCodes);
        codes.addAll(tCodes);
        codes.add(new IntervalHCode());

        return codes;
    }

    private LinkedList<Object> parseStyleObj() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to "style"

        nextToken();
        codes.addAll(parsePropertyList());

        codes.add(new StyleHCode());

        return codes;
    }

    private LinkedList<Object> parsePropertyList() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing that the current token points
        // to the start of the property list
        //
        // The property list can be empty. Otherwise, it can start with a
        // property element or an expression.

        boolean isPropElem = isName() && peek.isDelimiter() && peek.getChar() == ':';

        if (!isPropElem && !isExprStart()) {

            // Empty property list

            codes.add(0);
            codes.add(new PropertyListHCode());
            return codes;
        }

        int count = 0;

        while (true) {
            isPropElem = isName() && peek.isDelimiter() && peek.getChar() == ':';
            if (isPropElem) {

                // We have a property name

                codes.add(curToken.getValue());

                // Get the value

                nextToken();         // Current token is now ':'
                nextToken();         // Current toke is now start of expr
                codes.addAll(parseExpr());
                codes.add(new PropertyHCode());
            }
            else {
                codes.addAll(parseExpr());
            }

            count++;

            // If we don't find a comma, we're done

            if (!isDelimiter() || getChar() != ',') break;

            nextToken();
        }

        codes.add(count);
        codes.add(new PropertyListHCode());

        return codes;
    }

    private LinkedList<Object> parseExpr() throws ParseException
    {
        LinkedList<Object> codes = new LinkedList<>();

        // We start knowing only that an expr is expected
        // starting with the current token

        Stack<OpToken> ops = new Stack<>();
        int level = -1;
        ArrayList<Integer> argCount = new ArrayList<>();
        Token lastToken = null;

        // Let's check that this is the start of an expression

        if (!isExprStart()) {
            throwParseException("Expected an expression");
        }

        while (true) {

            // If we find a function name, push it on the operator stack.
            // Function names are always followed by a '('.

            if (isName() && peek.isDelimiter() && peek.getChar() == '(') {
                ops.push(new OpToken<>(curToken, Op.find('!', true)));
            }

            // If we have a variable, push it on the codes stack

            else if (isName()) {
                codes.add(curToken.getValue());
                if (lastToken == null || !lastToken.isOperator() || lastToken.getChar() != '.') {
                    codes.add(new FetchHCode());
                }
            }

            // If we have a number, push it on the codes stack

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

            // If we have a  comma, pop operators from the op stack to
            // the codes stack until we reach a "("

            else if (isDelimiter() && getChar() == ',') {
                if (level < 0) throw new ProgrammingException("Parser.parseExp(): Comma without preceding '('");

                while (!ops.isEmpty()) {
                    OpToken t = ops.pop();
                    if (t.op.chr != '(') {
                        codes.add(opTokenToHCode(t));
                    }
                    else {
                        ops.push(t);
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

            // If we have a "(", push it on the operator stack

            else if (isDelimiter() && getChar() == '(') {
               ops.push(new OpToken<>(curToken, Op.find('(', true)));
                level++;                    // Begin a new parenthesis level

                // Record the number of arguments seen so far (0)

                if (argCount.size() == level) {
                    argCount.add(0);
                }
                else {
                    argCount.set(level, 0);
                }
            }

            // We have a ")"

            else if (isDelimiter() && getChar() == ')') {
                if (level < 0) throwParseException("Unmatched ')'");

                // Bump the argument count by 1 unless the last token
                // was a '('

                if (lastToken != null && (!lastToken.isDelimiter() || lastToken.getChar() != '(')) {
                    argCount.set(level, argCount.get(level) + 1);
                }

                // Pop operators from the op stack to the codes stack until we
                // reach a "(". Then pop the "(" and discard it.

                while (!ops.isEmpty()) {
                    OpToken t = ops.pop();
                   if (t.op.chr != '(') {
                        codes.add(opTokenToHCode(t));
                    }
                    else {
                        break;
                    }
                }

                // If the operator at the top is now a function, pop it and push
                // the function name on the codes stack, along with the total of
                // all the arguments plus the function name

                if (ops.size() > 0 && ops.peek().op.chr == '!') {
                    OpToken t = ops.pop();
                    codes.add(t.token.getValue());
                    codes.add(argCount.get(level) + 1);
                    codes.add(new FunctionHCode());
                }

                // Handle coordinates, which look like functions with no names and
                // two arguments

                else if (argCount.get(level) == 2) {
                    codes.add(new CoordinateHCode());
                }

                // If we have any other argCount value at this point (other than
                // 1), we have an invalid expression

                else if (argCount.get(level) > 1) {
                   throwParseException("Invalid commas inside parentheses");
                }

                // Finished a level of parantheses

                level--;
            }

            // We have an operator

            else if (isOperator()) {

                // Special case unary +/-
                // Unary if at start of expression OR
                // following a comma OR
                // following '(' OR
                // following any other operator

                boolean isUnary =
                        lastToken == null ||
                        (lastToken.isDelimiter() && (lastToken.getChar() == '(' || lastToken.getChar() == ',')) ||
                        lastToken.isOperator();

                Op op = Op.find(getChar(), !isUnary);

                // Depending on precedence, we may first transfer some operators to
                // the codes stack

                while (ops.size() > 0) {
                    Op topOp = ops.peek().op;
                    if ((op.isLeftAssoc && op.precedence <= topOp.precedence) ||
                        (!op.isLeftAssoc && op.precedence < topOp.precedence)) {
                        OpToken topToken = ops.pop();
                        if (topToken.op.chr != '(') {
                            codes.add(opTokenToHCode(topToken));
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

                // Add the new operator to the operator stack

                ops.push(new OpToken<>(curToken, op));
            }

            // Since we are not using recursive descent to parse expressions,
            // we need to know when we're done with an expression
            //
            // An expression begins with a number, a string, a name, an open
            // paren ("(") or an open bracket ("["). Let's call these things
            // "items". It can also begin with a unary operator.
            //
            // When we finish parsing a name, we can follow it with an open paren.
            // When we finish parsing ANY item, we can follow it with an operator.
            // When we finish parsing an operator, we MUST follow it with an operator
            // or an item.
            // For our purposes, a parenthetical expression only counts when the
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
                    codes.add(opTokenToHCode(ops.pop()));
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
        // An expression begins with a number, a string, a name, an open
        // paren ("(") or an open bracket ("["). Let's call these things
        // "items". It can also begin with a unary operator.

        return isNumber() ||
               isString() ||
               isName() ||
               (isDelimiter() && (getChar() == '(' || getChar() == '[')) ||
               (isOperator() && (getChar() == '+' || getChar() == '-'));

    }

    private HCode opTokenToHCode(OpToken t)
    {
        switch (t.op.chr) {
            case '^' -> { return new ExpHCode(); }
            case '*' -> { return new MultHCode(); }
            case '/' -> { return new DivHCode(); }
            case '<' -> { return new InvLorentzHCode(); }
            case '>' -> { return new LorentzHCode(); }
            case '.' -> { return new FetchPropHCode(); }
            default -> {
            }
        }

        if (t.op.chr == '+' && t.op.isBinary) {
            return new AddHCode();
        } else if (t.op.chr == '-' && t.op.isBinary) {
            return new GenericHCode("sub");
        } else if  (t.op.chr == '+') {
            return new UnaryPlusHCode();
        } else if  (t.op.chr == '-') {
            return new UnaryMinusHCode();
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
        // If the current token is not EOF, the next token is a valid one,
        // so grab it (it might be EOF).

        peek = curToken;
        if (!isEOF()) peek = tokens.get(tokenPtr + 1);
    }

    private void returnToken()
    {
        // When we return a token, we want to set the tokens to what they
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
