/**
 * BooleanAlgebras
 * theory
 * Apr 21, 2015
 * @author Loris D'Antoni
 */

package theory;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * CharPred: a set of characters represented as contiguous intervals
 */
public class CharPred {

	/**
	 * The set containing only the character <code>c</code>
	 */
	public CharPred(char c) {
		this(c, c);
	}

	/**
	 * The set containing only the interval <code>[bot,top]</code> (extremes
	 * included)
	 */
	public CharPred(Character bot, Character top) {
		this(ImmutableList.of(ImmutablePair.of(bot, top)));
	}

	public static CharPred of(ImmutableList<Character> characters) {
		ImmutableList.Builder<ImmutablePair<Character, Character>> intervals = ImmutableList.builder();
		for (Character c : checkNotNull(characters)) {
			intervals.add(ImmutablePair.of(checkNotNull(c), c));
		}
		return new CharPred(intervals.build());
	}

	public CharPred(ImmutableList<ImmutablePair<Character, Character>> intervals) {
		for (ImmutablePair<Character, Character> interval : checkNotNull(intervals)) {
			checkArgument(interval.left != null && interval.right != null &&
					interval.left <= interval.right);
		}

		this.intervals = sortIntervals(checkNotNull(intervals));
	}

	private static ImmutableList<ImmutablePair<Character, Character>> sortIntervals(
			ImmutableList<ImmutablePair<Character, Character>> intervals) {
		for (ImmutablePair<Character, Character> interval : checkNotNull(intervals)) {
			checkArgument(interval.left != null && interval.right != null &&
					interval.left <= interval.right);
		}

		List<ImmutablePair<Character, Character>> sortLeft = new ArrayList<ImmutablePair<Character, Character>>(intervals);
		Collections.sort(sortLeft, new Comparator<ImmutablePair<Character, Character>>() {
				public int compare(ImmutablePair<Character, Character> o1,
						ImmutablePair<Character, Character> o2) {
					return checkNotNull(o1).left - checkNotNull(o2).left;
				}
			});

		ImmutableList.Builder<ImmutablePair<Character, Character>> ansBuilder = ImmutableList.builder();
		for (int i = 0; i < sortLeft.size(); i++) {
			char left = sortLeft.get(i).left;
			char right = sortLeft.get(i).right;
			for (int j = i + 1; j < sortLeft.size() && sortLeft.get(j).left <= right + 1; j++) {
				right = (char)Math.max(right, sortLeft.get(j).right);
				i++;
			}
			ansBuilder.add(ImmutablePair.of(left, right));
		}

		ImmutableList<ImmutablePair<Character, Character>> ans = ansBuilder.build();
		for (int i = 1; i < ans.size(); i++) {
			ImmutablePair<Character, Character> curr = ans.get(i);
			ImmutablePair<Character, Character> prev = ans.get(i - 1);
			checkArgument(prev.right < curr.left);
			checkArgument(prev.right + 1 < curr.left);
		}

		return ans;
	}

	public boolean isSatisfiedBy(char c) {
		for (ImmutablePair<Character, Character> interval : intervals) {
			if (interval.left <= c && c <= interval.right) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (ImmutablePair<Character, Character> pair : intervals) {
			if (pair.left == pair.right)
				sb.append(printChar(pair.left));
			else {
				sb.append(printChar(pair.left));
				sb.append("-");
				sb.append(printChar(pair.right));
			}
		}
		sb.append("]");

		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CharPred) {
			return Objects.equals(intervals, ((CharPred)obj).intervals);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(CharPred.class, intervals);
	}

	public final ImmutableList<ImmutablePair<Character, Character>> intervals;

	public static final char MIN_CHAR = Character.MIN_VALUE;
    public static final char MAX_CHAR = Character.MAX_VALUE;

	// Only prints readable chars, otherwise print unicode
	public static String printChar(char c) {
		Map<Character, String> unescapeMap = new HashMap<Character, String>();
		unescapeMap.put('\t', "\\t");
		unescapeMap.put('\b', "\\b");
		unescapeMap.put('\n', "\\n");
		unescapeMap.put('\r', "\\r");
		unescapeMap.put('\f', "\\f");
		unescapeMap.put('\'', "\\\'");
		unescapeMap.put('\"', "\\\"");
		if (unescapeMap.containsKey(c)) {
			return unescapeMap.get(c);
		} else if (c < 0x20 || c > 0x7f) {
			return String.format("\\u%04x", (int) c);
		} else {
			return Character.toString(c);
		}
	}

}
