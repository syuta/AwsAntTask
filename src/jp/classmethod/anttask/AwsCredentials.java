package jp.classmethod.anttask;

/**
 * AWS認証情報を扱うBean.
 * @author nakamura.shuta
 *
 */
public class AwsCredentials {

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	private String accessKey;
	private String secretKey;

}
