package jp.classmethod.anttask;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

/**
 * Amazon S3へアップロードするためのAntタスク.
 * 
 * @author nakamura.shuta
 * 
 */
public class S3Upload extends Task {

	private Log log = LogFactory.getLog(S3Upload.class);
	/** AWS認証情報 */
	private AwsCredentials awsCredentials;

	/** バケット */
	private String bucket;

	/** prefix */
	private String prefix;

	/** contentEncoding */
	private String contentEncoding;

	/** contentType */
	private String contentType;

	/** Cache-Control */
	private String cacheControl;

	/** build.xmlで指定されたFileSet等の一覧 */
	protected List<ResourceCollection> rclist = new ArrayList<ResourceCollection>();

	/** S3クライアント */
	AmazonS3Client s3;

	public void execute() throws BuildException {
		log.info("S3Upload#execurte");
		// プロパティ検証
		validateProperties();
		// ログ出力
		showInfoMessage();
		// AWS認証
		BasicAWSCredentials credentials = new BasicAWSCredentials(awsCredentials.getAccessKey(),
				awsCredentials.getSecretKey());
		// S3オブジェクト作成
		s3 = new AmazonS3Client(credentials);
		for (ResourceCollection rc : rclist) {
			for (Iterator<?> i = rc.iterator(); i.hasNext();) {
				Resource r = (Resource) i.next();
				if (r instanceof FileResource) {
					FileResource fr = (FileResource) r;
					File f = fr.getFile();
					f = new File(fr.getBaseDir(), fr.getName());
					uploadObject(fr.getName(), f);
				}
			}
		}
	}

	/**
	 * 情報出力.
	 */
	private void showInfoMessage() {
		log.info("upload bucket:" + bucket);
		log.info("prefix:" + prefix);
		log.info("contentType:" + contentType);
		log.info("contentEncoding:" + contentEncoding);
		log.info("upload file list:" + rclist);
	}

	/**
	 * 認証情報のチェック.
	 */
	private void validateProperties() {
		if (awsCredentials.getAccessKey() == null || awsCredentials.getSecretKey() == null) {
			throw new BuildException("AwsCredentialsが正しくありません.");
		}

		if (bucket == null) {
			throw new BuildException("バケット名が正しくありません.");
		}

	}

	/**
	 * ファイルをアップロードする.
	 * 
	 * @param baseDir
	 *            ローカルファイルのパス(filesetで指定した箇所から）
	 * @param uploadFile
	 *            　アップロードするファイル
	 */
	private void uploadObject(String baseDir, File uploadFile) {
		String key = (prefix + "/" + baseDir).replace('\\', '/');
		PutObjectRequest obj = new PutObjectRequest(bucket, key, uploadFile);
		ObjectMetadata meta = new ObjectMetadata();
		if (contentEncoding != null) {
			meta.setContentEncoding(contentEncoding);
		}
		if (contentType != null) {
			meta.setContentType(contentType);
		}
		if (cacheControl != null) {
			meta.setCacheControl(cacheControl);
		}
		obj.setMetadata(meta);
		PutObjectResult res = s3.putObject(obj);
		AccessControlList acl = s3.getBucketAcl(bucket);
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
		s3.setObjectAcl(bucket, key, acl);
		log.info(baseDir + "のファイルを," + key + "にアップロード.");
	}

	public void add(ResourceCollection rc) {
		rclist.add(rc);
	}

	public void addConfiguredAwsCredentials(AwsCredentials param) {
		this.awsCredentials = param;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

}
