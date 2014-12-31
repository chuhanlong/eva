package serverT;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;

import com.lashou.common.util.JsonUtil;

public class MD5Test {

	  public String md5Encrypted(String key, Object... datium)
				throws NoSuchAlgorithmException, UnsupportedEncodingException {
				MessageDigest md = MessageDigest.getInstance("MD5");
				StringBuilder sb = new StringBuilder("");
				if(datium != null && datium.length > 0){
					for(Object data: datium){
						if(data != null){
							sb.append(data.toString());
						}
					}
				}
				sb.append(key);
				System.out.println("MD5：原始串为"+sb.toString());
				md.update(sb.toString().getBytes());
				byte b[] = md.digest();
				int i;
				StringBuffer buf = new StringBuffer("");
				for (int offset = 0; offset < b.length; offset++) {
					i = b[offset];
					if (i < 0){
						i += 256;
					}
					if (i < 16) {
						buf.append("0");
					}
					buf.append(Integer.toHexString(i));
				}
				System.out.println("MD5：校验串为"+buf.toString());
				return buf.toString();
		    }
	  
	  public static void main(String[] args) throws Exception, UnsupportedEncodingException {
		  String name = "maxpool12";
		  String password = "c4ca4238a0b923820dcc509a6f75849b";
		  MD5Test md5 = new MD5Test();
		  String pa = md5.md5Encrypted("test", 1417598018);
		  String pas = md5.md5Encrypted("", name, password, pa);
		  System.out.println(pas);
	}
}
