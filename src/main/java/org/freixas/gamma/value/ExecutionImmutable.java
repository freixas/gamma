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
package gamma.value;

/**
 * This interface signals that this is an object that can appear in the
 * data portion of an HCodeProgram, but cannot be changed during execution of
 * the program. It can be changed between executions of the same program.
 * <p>
 * Object properties must either be final or ExecutionImmutable.
 *
 * @author Antonio Freixas
 */
public interface ExecutionImmutable
{

}
