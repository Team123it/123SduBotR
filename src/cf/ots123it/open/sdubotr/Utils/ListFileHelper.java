package cf.ots123it.open.sdubotr.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import cf.ots123it.jhlper.IOHelper;

/**
 * 列表文件辅助操作类
 * @author 御坂12456
 *
 */
public class ListFileHelper  {
	/**
	 * 列表文件路径
	 */
	String filePath = null;
	/**
	 * 创建一个新的列表文件辅助操作实例。
	 * @param path 列表文件的路径。
	 */
	public ListFileHelper(String path) {
		try {
			if (!path.trim().equals("")) { //如果去掉首尾空格后路径不为空
				this.filePath = path.trim(); //设置路径
			} else { //如果去掉首尾空格后路径为空
				this.filePath = null;
			}
		} catch (NullPointerException e) { //空指针异常捕获
			this.filePath = null;
		}
	}
	/**
	 * 创建一个新的列表文件辅助操作实例。
	 * @param file 列表文件的对象。
	 */
	public ListFileHelper(File file) {
		try {
			if (!file.equals(null)) { //如果文件对象不为null
				if (!file.getAbsolutePath().equals("")) { //如果文件路径不为空
					this.filePath = file.getAbsolutePath(); //设置路径
				} else { //如果文件路径为空
					this.filePath = null;
				}
			} else { //如果文件对象为null
				this.filePath = null;
			}
		} catch (NullPointerException e) { //空指针异常捕获
			this.filePath = null;
		}
	}
	/**
	 * 创建一个空的新列表文件辅助操作实例。
	 */
	public ListFileHelper() {
		this.filePath = null; //初始化路径为null
	}
	/**
	 * 初始化本实例的列表文件对象。
	 * @param file 要初始化的列表文件的对象
	 */
	public void setFile(File file) {
		try {
			if (!file.equals(null)) { //如果文件对象不为null
				if (!file.getAbsolutePath().equals("")) { //如果文件路径不为空
					this.filePath = file.getAbsolutePath(); //设置路径
				} else { //如果文件路径为空
					this.filePath = null;
				}
			} else { //如果文件对象为null
				
			}
		} catch (NullPointerException e) { //空指针异常捕获
			this.filePath = null; 
		}
	}
	/**
	 * 初始化本实例的列表文件路径。
	 * @param path 要初始化的列表文件路径
	 */
	public void setFilePath(String path) {
		try {
			if (!path.trim().equals("")) { //如果去掉首尾空格后路径不为空
				this.filePath = path.trim(); //设置路径
			} else { //如果去掉首尾空格后路径为空
				this.filePath = null;
			}
		} catch (NullPointerException e) { //空指针异常捕获
			this.filePath = null;
		}
	}
	/**
	 * 获取本实例的列表文件对象
	 * @return 列表文件对象
	 */
	public File getFile()
	{
		try {
			if (!this.filePath.equals("")) { //如果路径不为空
				return new File(this.filePath); //返回新的文件对象
			} else { //如果路径为空
				return null;
			}
		} catch (NullPointerException e) { //空指针异常捕获
			return null;
		}
	}
	/**
	 * 获取本实例的列表文件路径
	 * @return 列表文件路径
	 */
	public String getFilePath()
	{
		try {
			return this.filePath; //返回路径
		} catch (NullPointerException e) { //空指针异常捕获
			return null;
		}
	}
	/**
	 * 获取列表文件中的列表数组并返回
	 * @return 字符串列表数组，若读取结果为空则返回null
	 * @throws ListFileException 读取过程中或转成字符串数组时出现错误时抛出此异常
	 * @throws NullPointerException 未初始化实例中的文件信息(其为null)时抛出此异常
	 */
	public ArrayList<String> getList() throws ListFileException, NullPointerException {
		try {
			if (!this.filePath.equals(null)) { //如果本实例的文件路径不为空
				File listFile = new File(this.filePath); //创建列表文件实例
				if (listFile.exists()) { //如果列表文件存在
					String listDataStr = IOHelper.ReadToEnd(listFile); //读取完整文件内容
					if (!listDataStr.trim().equals("")) { //如果文件内容不为空
						String[] listData = IOHelper.ReadAllLines(listFile); //读取完整文件数据数组（每行为一个项）
						ArrayList<String> returnData = new ArrayList<String>(); //创建返回的真正数组实例
						for (String listDataSingle : listData) { //循环完整文件数据数组的每一项（listDataSingle）
							if (!listDataSingle.trim().equals("")) { //如果该项不为空且不仅空格
								returnData.add(listDataSingle.trim()); //添加去除首尾空格的项至真正数组
							}
						}
						return returnData; //返回真正数组
					} else { //如果文件内容为空（实际为空或只有空格）
						return null; //返回null
					}
				} else { //如果列表文件不存在
					throw new ListFileException("The file isn't exist:" + this.filePath);
				}
			} else { //如果本实例的文件路径为空
				throw new NullPointerException("Please define list file path or a list file object with certain file path!");
			}
		} catch (IndexOutOfBoundsException e) { //下标越界异常捕获
			throw new ListFileException("数据下标越界，请检查列表文件内容是否格式出现问题");
		} catch (Exception e) { //常规异常捕获
			throw new ListFileException("Unknown Exception:" + e.getLocalizedMessage());
		}
	}
	/**
	 * 向列表文件中创建或追加数据项。<br>
	 * 如果本实例的列表文件不存在，将自动创建该文件并写入数据项。<br>
	 * @param str 要追加的字符串
	 * @return 成功返回0，数据出现重复返回1，失败返回-1
	 * @throws ListFileException 追加的字符串为空时抛出此异常
	 */
	public int add(String str) throws ListFileException
	{
		try {
			if ((!str.trim().equals(null) || (!str.trim().equals("")))) { //如果要追加的不是空字符串
				File listFile = new File(this.filePath); //创建列表文件实例
				if (listFile.exists()) { //如果列表文件存在
					if (IOHelper.ReadToEnd(listFile).equals("")) { //如果列表文件为空
						IOHelper.WriteStr(listFile, str); //直接写入数据
						return 0; //返回成功（0）
					} else { //否则（不为空）
						for (String listSingleData : IOHelper.ReadAllLines(listFile)) {
							if ((listSingleData.equals(null)) || (listSingleData.equals(""))) { //如果列表中当前遍历到的数据为空
								continue; //进行下一次循环
							} else if (str.equals(listSingleData)) { //如果要追加的数据在列表中已存在
								return 1; //返回已存在（1）
							}
						}
						IOHelper.AppendWriteStr(listFile, "\n" + str); //追加写入数据(回车符+数据)
						return 0; //返回成功（0）
					}
				} else { //如果列表文件不存在
					IOHelper.WriteStr(listFile, str); //创建文件并写入数据
					return 0; //返回成功（0）
				}
			} else { //如果要追加的是空字符串
				throw new ListFileException("Cannot write(create or append) blank sum");
			}
		}
		catch (NullPointerException e) { //空指针异常捕获
			return -1; //返回失败（-1）
		}
	}
	/**
	 * 从列表文件中移除数据项。<br>
	 * @param str 要移除的字符串
	 * @return 成功返回0，未找到对应项返回1，列表文件为空或者不存在返回2，失败（其它异常）返回-1
	 * @throws ListFileException 要移除的字符串为空时抛出此异常
	 */
	public int remove(String str) throws ListFileException
	{
		try {
			if ((!str.trim().equals(null) || (!str.trim().equals("")))) { //如果要移除的不是空字符串
				File listFile = new File(this.filePath); //创建列表文件实例
				if (listFile.exists()) { //如果列表文件存在
					if (IOHelper.ReadToEnd(listFile).trim().equals("")) { //如果列表文件为空
						return 2; //返回列表文件为空或者不存在（2）
					} else { //否则（不为空）
						for (String listSingle : IOHelper.ReadAllLines(listFile)) {
							if (str.equals(listSingle)) { //如果指定项在列表里
								ArrayList<String> newListData = new ArrayList<String>(); //新建一个ArrayList对象
								Collections.addAll(newListData, IOHelper.ReadAllLines(listFile)); //读取原列表所有项
								/* 由于直接remove(Object o)判断的是地址导致无法remove成功
								 * 故此处直接使用ArrayList的父类迭代器的remove方法来remove掉指定项
								 * （确定的ArrayList对应的迭代器若发生更改，该ArrayList也会发生更改）
								 * Author:御坂12456 于 2020-2-15 0:36 */
								Iterator<String> it = newListData.iterator(); //获取迭代器对象
								while (it.hasNext()) { //如果它存在下一个项的值
									String tmpSum = it.next(); //获取下一个项的值
									if (tmpSum.equals(str)) { //如果下一个项的值与要移除的项的值一致
										it.remove(); //移除该项
									}
								}
								listFile.delete(); //先删除原列表文件
								IOHelper.WriteStr(listFile, ""); //重新创建该文件
								for (String newListSingle : newListData) {
									if (newListSingle.equals(newListData.get(newListData.size() - 1))) { //如果当前循环到的newListSingle是最后一个
										IOHelper.AppendWriteStr(listFile, newListSingle); //直接写入最后一项
										break; //跳出for循环
									} else {
										IOHelper.AppendWriteStr(listFile, newListSingle  + "\n"); //写入后来个换行
									}
								}
								return 0; //返回成功（0）
							}
						}
						return 1; //返回未找到指定项（1）
					}
				} else { //如果列表文件不存在
					return 2; //返回列表文件为空或者不存在（2）
				}
			} else { //如果要移除的字符串为空
				throw new ListFileException("Cannot write(remove) blank sum");
			}
		} catch (NullPointerException e) { //空指针异常捕获
			return -1; //返回失败（-1）
		} catch (IndexOutOfBoundsException e) { //数组索引超出下限异常捕获
			return -1; //返回失败（-1）
		}
	}
	/**
	 * 释放本实例列表文件类所占用的所有内存资源。
	 * @throws Throwable 
	 */
	@Override
	public void finalize() {
		this.filePath = null;
	}
}

