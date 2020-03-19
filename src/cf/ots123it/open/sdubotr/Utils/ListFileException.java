/**
 * 
 */
package cf.ots123it.open.sdubotr.Utils;

/**
 * 列表文件I/O异常类
 * @author 御坂12456
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