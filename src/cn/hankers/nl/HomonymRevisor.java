package cn.hankers.nl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class HomonymRevisor {

	private final static String TAG = "HomonymRevisor";

	private boolean _bFuzzyEnabled = true;
	private AhoCorasickDoubleArrayTrie<String> _acdat;

	public static void main(String[] args) {

		// ׼����ҵ�ʵ�
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("abakawei", "���Ϳ�Τ");
		map.put("abeiaiershishoushu", "��������������");
		map.put("abendazuo", "��������");
		map.put("aerrezuerxuehongdanbai", "���������Ѫ�쵰��");
		map.put("xueyangbaohedu", "Ѫ�����Ͷ�");
		map.put("tangshizonghezheng", "�����ۺ�֢");

		// �������
		final String origin = ".���˵�Ѫ�����Ͷ���64%,�ϣ������������к���Σ����!";

		HomonymRevisor revisor = new HomonymRevisor(map, true);
		
		// У�����
		final String revised = revisor.revise(origin);

		log(TAG, origin + "=>" + revised);
	}

	private static void log(String tag, String msg) {
		System.out.print(tag + "  " + msg + "\r\n");
	}

	@SuppressWarnings("unused")
	private HomonymRevisor() {
	}

	public HomonymRevisor(Map<String, String> dict, boolean fuzzyEnabled) {
		_bFuzzyEnabled = fuzzyEnabled;

		TreeMap<String, String> map = new TreeMap<String, String>();
		for (Map.Entry<String, String> entry : dict.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			String calcuatedPy = calcFuzzyPinYinForString(val, key);
			if (!map.containsKey(calcuatedPy)) {
				map.put(calcuatedPy, val);
			} else {
				log(TAG, "conflict item found:" + calcuatedPy + " for " + val);
			}
		}

		// Build an AhoCorasickDoubleArrayTrie
		_acdat = new AhoCorasickDoubleArrayTrie<String>();
		_acdat.build(map);
	}

	public String revise(String chineseStr) {
		ArrayList<String> sentences = splite2Sentence(chineseStr);
		log(TAG, sentences.toString());

		StringBuilder builder = new StringBuilder();

		for (String sentence : sentences) {
			if (sentence.length() > 1) {
				builder.append(correctSentence(sentence));
			} else {
				builder.append(sentence);
			}
		}
		return builder.toString();
	}

	private String getSubstringByPinYinPosition(final String src, int pyStart, int pyEnd, int[] pyLengthList) {
		int cumulus = 0;
		int startIndex = 0;
		int endIndex = 0;
		for (int i = 0; i < pyLengthList.length; ++i) {
			cumulus += pyLengthList[i];
			if (cumulus == pyStart) {
				startIndex = i + 1;
			} else if (cumulus == pyEnd) {
				endIndex = i + 1;
			} else if (cumulus > pyEnd) {
				break;
			}
		}
		return src.substring(startIndex, endIndex);
	}

	private int minEditDistance(String word1, String word2) {
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);

				// if last two chars equal
				if (c1 == c2) {
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;

					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}

		return dp[len1][len2];
	}

	private ArrayList<String> splite2Sentence(String src) {
		ArrayList<String> sentences = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < src.length(); ++i) {
			char c = src.charAt(i);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
			if (pinyinArray == null) {
				if (start == i) {
					sentences.add(src.substring(start, i + 1));
				} else {
					sentences.add(src.substring(start, i));
					sentences.add(src.substring(i, i + 1));
				}
				start = i + 1;
			}
		}
		if (start < src.length() - 1) {
			sentences.add(src.substring(start, src.length()));
		}
		return sentences;
	}

	private String correctSentence(String origin) {
		int[] pyLengthList = new int[origin.length()];
		String[] pyList = new String[origin.length()];
		StringBuilder builder = new StringBuilder();
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		try {
			int idx = 0;
			for (char c : origin.toCharArray()) {
				String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, outputFormat);
				if (pinyinArray != null) {
					String calcutedPy = calcFuzzyPinYinForCharacter(pinyinArray[0]);
					pyLengthList[idx] = calcutedPy.length();
					pyList[idx] = calcutedPy;
					builder.append(calcutedPy);
				} else {
					pyLengthList[idx] = 1;
					pyList[idx] = String.valueOf(c);
					builder.append(c);
				}
				idx++;
			}
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			e1.printStackTrace();
		}

		String result = origin;
		final String text = builder.toString();
		log(TAG, text);
		List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> wordList = _acdat.parseText(text);
		for (AhoCorasickDoubleArrayTrie<String>.Hit<String> hit : wordList) {
			String originSub = getSubstringByPinYinPosition(origin, hit.begin, hit.end, pyLengthList);
			log(TAG, String.format("parseText=%d,%d,%s,original=%s", hit.begin, hit.end, hit.value, originSub));
			if (minEditDistance(originSub, hit.value) < 3) {
				result = result.replace(originSub, hit.value);
			}
		}
		log(TAG, "Result=" + result);
		return result;
	}

	private String calcFuzzyPinYinForString(String hanz, String originPinyin) {
		if (!_bFuzzyEnabled)
			return originPinyin;

		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

		ArrayList<String> list = new ArrayList<>();

		int offset = 0;

		for (int i = 0; i < hanz.length(); ++i) {
			char c = hanz.charAt(i);
			try {
				String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, outputFormat);
				if (pinyinArray != null) {
					boolean found = false;
					for (String py : pinyinArray) {
						if (originPinyin.startsWith(py, offset)) {
							list.add(py);
							offset += py.length();
							found = true;
							break;
						}
					}
					if (!found) {
						list.add(pinyinArray[0]);
						offset += pinyinArray[0].length();
					}
				} else {
					list.add(String.valueOf(c));
				}
			} catch (BadHanyuPinyinOutputFormatCombination e1) {
				e1.printStackTrace();
			}
		}

		StringBuilder builder = new StringBuilder();

		for (String itm : list) {
			String temp = calcFuzzyPinYinForCharacter(itm);
			builder.append(temp);
		}

		return builder.toString();
	}

	private String calcFuzzyPinYinForCharacter(String pinyin) {
		if (!_bFuzzyEnabled)
			return pinyin;

		String temp = pinyin;

		if (temp.startsWith("sh")) {
			temp = "s" + temp.substring(2);
		} else if (temp.startsWith("ch")) {
			temp = "c" + temp.substring(2);
		} else if (temp.startsWith("zh")) {
			temp = "z" + temp.substring(2);
		} else if (temp.startsWith("n")) {
			temp = "l" + temp.substring(1);
		} else if (temp.startsWith("r")) {
			temp = "l" + temp.substring(1);
		} else if (temp.startsWith("h")) {
			temp = "f" + temp.substring(1);
		}

		if (temp.endsWith("iang")) {
			temp = temp.substring(0, temp.length() - 4) + "ian";
		} else if (temp.endsWith("uang")) {
			temp = temp.substring(0, temp.length() - 4) + "uan";
		} else if (temp.endsWith("ang")) {
			temp = temp.substring(0, temp.length() - 3) + "an";
		} else if (temp.endsWith("eng")) {
			temp = temp.substring(0, temp.length() - 3) + "en";
		} else if (temp.endsWith("ing")) {
			temp = temp.substring(0, temp.length() - 3) + "in";
		}

		return temp;
	}
}
