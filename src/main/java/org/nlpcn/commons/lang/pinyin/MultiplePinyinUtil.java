/**
 * File    : Pinyin.java
 * Created : 2014年1月22日
 * By      : luhuiguo
 */
package org.nlpcn.commons.lang.pinyin;

import org.nlpcn.commons.lang.tire.SmartGetWord;
import org.nlpcn.commons.lang.tire.domain.SmartForest;
import org.nlpcn.commons.lang.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @author luhuiguo
 * @author ansj
 */
enum MultiplePinyinUtil {

	INSTANCE;

	public static final String PINYIN_MAPPING_FILE = "/pinyin.txt";

	public static final String EMPTY = "";
	public static final String SHARP = "#";
	public static final String EQUAL = "=";
	public static final String COMMA = ",";
	public static final String SPACE = " ";

	private SmartForest<String[]> polyphoneDict = null;

	private int maxLen = 2;

	MultiplePinyinUtil() {
        polyphoneDict = new SmartForest<String[]>();
        loadPinyinMapping();
	}

	public void loadPinyinMapping() {

		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new BufferedInputStream(getClass().getResourceAsStream(PINYIN_MAPPING_FILE)), StandardCharsets.UTF_8));
			String line = null;
			while (null != (line = in.readLine())) {
				if (line.length() == 0 || line.startsWith(SHARP)) {
					continue;
				}
				String[] pair = line.split(EQUAL);
				if(pair.length==2&&StringUtil.isNotBlank(pair[1])){
					polyphoneDict.add(pair[0], pair[1].split(","));
				}

			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public List<String> convert(String str, PinyinFormatter.TYPE format) {

		if (StringUtil.isBlank(str)) {
			return Collections.emptyList();
		}

		SmartGetWord<String[]> word = polyphoneDict.getWord(str);

		List<String> lists = new LinkedList<String>();

		String temp = null;
		int beginOffe = 0;
		while ((temp = word.getFrontWords()) != null) {

			for (int i = beginOffe; i < word.offe; i++) {
				lists.add(null);
			}

			if(temp.length()==1){ //多音字用" "链接
//				lists.add(PinyinFormatter.formatPinyin(word.getParam()[0], format));
//              System.out.println(String.join(" ", word.getParam()));

				String[] filterParam = filterParams(word.getParam(), format);
                lists.add(String.join(" ", filterParam));

            }else{
				for (String t : word.getParam()) {
					lists.add(PinyinFormatter.formatPinyin(t, format));

				}
			}

			beginOffe = word.offe + temp.length();
		}

		if (beginOffe < str.length()) {
			for (int i = beginOffe; i < str.length(); i++) {
				lists.add(null);
			}
		}


		return lists;

	}

	public String[] filterParams(String[] params, PinyinFormatter.TYPE format) {

        Set<String> set = new LinkedHashSet<>();
        List<String> newParams = new ArrayList<>();

        for (String param: params) {
            int lenBefore = set.size();
            String formatParam = PinyinFormatter.formatPinyin(param, format);
            set.add(formatParam);
            if (lenBefore != set.size()) {
                newParams.add(formatParam);
            }
        }
		return newParams.toArray(new String[0]);
    }

}
