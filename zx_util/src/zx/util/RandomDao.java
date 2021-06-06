package zx.util;

import java.util.Random;
/**
 * 计算随机事件概率 
 * */
public class RandomDao {
	private Random random = new Random();

	/**
	 * 单次事件是否发生
 * @param percent 事件发生的概率
 * */
	public boolean happen(int percent) {
		if (percent <= 0) {
			return false;
		}
		if (percent >= 100) {
			return true;
		}
		int result = random.nextInt(100) + 1;// 1~100
		if (result <= percent)
			return true;
		return false;
	}
	
//	public static void main(String[] args) {
//		RandomDao dao = new RandomDao();
//		float jugg = 0;
//		float pa = 0;
//		int jugghit = 250, pahit = 250;
//		for(int i = 0;i<10;i++){
//			jugg +=dao.happen(35)?(2*jugghit):jugghit;
//			pa+=dao.happen(15)?(4.5*pahit):pahit;
//		}
//		System.out.println("jugg:"+jugg+"  pa:"+pa);
//		
//	}
}
