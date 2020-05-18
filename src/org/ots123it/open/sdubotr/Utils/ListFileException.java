/**
 * 
 */
package org.ots123it.open.sdubotr.Utils;

/**
 * 列表文件I/O异常类
 * @author 御坂12456
 * @deprecated 自0.5.0起不再使用列表文件记录数据，如非必要，不建议使用该类。请改用{@link java.sql.SQLException}。
 * @version 从0.2.5
 */
public class ListFileException extends Exception {
	private static final long serialVersionUID = 1L;
		@SuppressWarnings("unused")
		private String message = null;
		public ListFileException(String message){
			super(message);
			this.message = message;
		}
	}