/* 
 * Copyright (C) 2017 Laurens Weyn
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
package language.deconjugator;

import language.dictionary.DefTag;

import java.util.ArrayList;

/**
 * Holds a rule for deconjugating a word
 * @author Laurens Weyn
 */
public interface DeconRule
{
    /**
     * Attempts to deconjugate a word with this
     * @param word the word to attempt to deconjugate
     * @return the deconjugated word, or null if it doesn't apply
     */
    public abstract ValidWord process(ValidWord word);
}
