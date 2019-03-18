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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author luhuiguo
 * @author ansj
 */
enum PinyinUtil {

	INSTANCE;

	public static final String PINYIN_MAPPING_FILE = "/pinyin.txt";
	public static final String POLYPHONE_MAPPING_FILE = "/polyphone.txt";

	public static final String EMPTY = "";
	public static final String SHARP = "#";
	public static final String EQUAL = "=";
	public static final String COMMA = ",";
	public static final String SPACE = " ";

	private SmartForest<String[]> polyphoneDict = null;

	private int maxLen = 2;

	PinyinUtil() {
		polyphoneDict = new SmartForest<String[]>();
		loadPinyinMapping();
		loadPolyphoneMapping();
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

	public void loadPolyphoneMapping() {


		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new BufferedInputStream(getClass().getResourceAsStream(POLYPHONE_MAPPING_FILE)), StandardCharsets.UTF_8));

			String line = null;
			while (null != (line = in.readLine())) {
				// line = line.trim();
				if (line.length() == 0 || line.startsWith(SHARP)) {
					continue;
				}
				String[] pair = line.split(EQUAL);

				if (pair.length < 2) {
					continue;
				}
				maxLen = maxLen < pair[0].length() ? pair[0].length() : maxLen;

				polyphoneDict.add(pair[0], pair[1].split(SPACE));

			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> convert(String str,  PinyinFormatter.TYPE format) {

		if (StringUtil.isBlank(str)) {
			return Collections.emptyList();
		}

		SmartGetWord<String[]> word = polyphoneDict.getWord(str);

		List<String> lists = new LinkedList<String>();

		String temp = null;
		int beginOffe = 0;
        List<List<String>> intraLists = new LinkedList<>();
		while ((temp = word.getFrontWords()) != null) {
			if (beginOffe != word.offe) {
			    for (List<String> list : intraLists) {
                    lists.add(null);
			        lists.addAll(list);
                }
                intraLists = new LinkedList<>();
            }

            String[] param = word.getParam();
			int intraSize = intraLists.size();
            if (intraSize > 0) {
                if (param.length > 1) {
                    for (int i=0; i<intraSize; i++) {
                        List<String> list = new LinkedList<String>();
                        list.addAll(intraLists.get(i));

                        for (int j = 0; j < param.length; j ++) {
                            list.add(PinyinFormatter.formatPinyin(param[j], format));
                            intraLists.add(((LinkedList<String>) ((LinkedList<String>) list).clone()));
                            list.remove(list.size() - 1);
                        }

                    }
                    for (int i=0; i<intraSize; i++) {
                        intraLists.remove(intraSize-i-1);
                    }
                } else {
                    for (int i = 0; i<intraLists.size(); i++) {
                        intraLists.get(i).add(PinyinFormatter.formatPinyin(param[0], format));
                    }
                }
            } else {
                if (param.length > 1) {
                    for (int j = 0; j < param.length; j ++) {
                        LinkedList<String> newList  = new LinkedList<String>();
                        newList.add(PinyinFormatter.formatPinyin(param[j], format));
                        intraLists.add(newList);
                    }
                } else {
                    LinkedList<String> newList  = new LinkedList<String>();
                    newList.add(PinyinFormatter.formatPinyin(param[0], format));
                    intraLists.add(newList);
                }
            }
            beginOffe = word.offe + temp.length();
		}

        for (List<String> list : intraLists) {
            lists.addAll(list);
            lists.add(null);
        }


		return lists;

	}

	/**
	 * 动态增加拼音到词典
	 *
	 * @param word
	 * @param pinyins
	 */
	public void insertPinyin(String word, String[] pinyins) {
		polyphoneDict.add(word, pinyins);
	}
}
