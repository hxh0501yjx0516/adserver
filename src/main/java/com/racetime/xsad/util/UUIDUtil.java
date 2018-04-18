package com.racetime.xsad.util;

import java.util.UUID;

/**
 * 
* 项目名称：ad-service   
* 类名称：UUIDUtil   
* 类描述：   获取UUID
* 创建人：skg   
* 创建时间：2017-7-17 上午10:46:18   
* @version    
*
 */
public class UUIDUtil {
	/**
	 * 获取32位uuid
	 * @return
	 */
	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	// 要使用生成 URL 的字符
	static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h",
			"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
			"u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
			"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };
	
	/**
	 * 获取8位短id
	 * @return
	 */
	public static String generateShortUuid() {
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();
	}
	public static void main(String[] args) {
		System.out.println(getUuid());
		System.out.println(generateShortUuid());
		System.out.println(UUID.randomUUID());
	}
}
