package test1;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.ac.caict.iiiiot.idisc.core.BaseResponse;
import cn.ac.caict.iiiiot.idisc.core.IdentifierException;
import cn.ac.caict.iiiiot.idisc.core.IdisCommunicationItems;
import cn.ac.caict.iiiiot.idisc.core.ResolutionResponse;
import cn.ac.caict.iiiiot.idisc.core.ServerInfo;
import cn.ac.caict.iiiiot.idisc.core.SiteInfo;
import cn.ac.caict.iiiiot.idisc.core.SiteResponse;
import cn.ac.caict.iiiiot.idisc.data.AdminInfo;
import cn.ac.caict.iiiiot.idisc.data.IdentifierValue;
import cn.ac.caict.iiiiot.idisc.data.MsgSettings;
import cn.ac.caict.iiiiot.idisc.data.SignatureInfo;
import cn.ac.caict.iiiiot.idisc.data.ValueReference;
import cn.ac.caict.iiiiot.idisc.security.Permission;
import cn.ac.caict.iiiiot.idisc.service.IChannelManageService;
import cn.ac.caict.iiiiot.idisc.service.IIDManageServiceChannel;
import cn.ac.caict.iiiiot.idisc.service.impl.ChannelManageServiceImpl;
import cn.ac.caict.iiiiot.idisc.utils.IdentifierValueUtil;
import cn.ac.caict.iiiiot.idisc.utils.Util;

public class TestManageConnection {
	public static final int CHANNEL_CLOSED = 0;
	public static final int CHANNEL_LOGIN = 1;
	public static final int CHANNEL_LOGOUT = 2;

	public static void main(String[] args) throws Exception {
		// 创建通道管理实例
		IChannelManageService chnnlService = new ChannelManageServiceImpl();
		try {
			// 根据IDIS系统提供的ip和端口，创建与IDIS的连接通道对象
			IIDManageServiceChannel channel = chnnlService.generateChannel("192.168.150.25", 2640, "TCP");
			// testLookup(channel);
			// testGetSiteInfo(channel);
			if (channel != null && chnnlService.getIDManageServiceChannelState(channel) == CHANNEL_LOGOUT) {
				String userId0 = chnnlService.getChannelUserIdentifier(channel);
				System.out.println("++++++" + userId0);
				 BaseResponse loginResp = testLogin(channel);
				 if (loginResp != null && loginResp.responseCode == 1) {
				 System.out.println("登录成功!");
				// testDelete(channel);
				// testCreate(channel);
				testAdd(channel);
				// testEdit(channel);
				// testRemove(channel);
				}
				// testLookup(channel);
			}
		} catch (IdentifierException e) {
			e.printStackTrace();
		}
	}

	private static BaseResponse testLogin(IIDManageServiceChannel channel) throws IdentifierException {
		return channel.login("88.1000.1/fy", 1, "D:\\工作\\客户端-无登录\\hsclient_new_lhs_resolver\\bin\\rsa_pri.bin", null,
				1);
	}

	private static BaseResponse testDelete(IIDManageServiceChannel channel) throws IdentifierException {
		return channel.deleteIdentifier("88.1000.2/cupA", new MsgSettings());
	}

	private static BaseResponse testCreate(IIDManageServiceChannel channel) throws IdentifierException {
		IdentifierValue value1 = new IdentifierValue();
		try {
			value1 = makeSiteInfoValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IdentifierValue value2 = makeVListValue();

		IdentifierValue value3 = new IdentifierValue(500, "HS_SERV", "88.1234");

		return channel.createIdentifier("88.1234.1234", new IdentifierValue[] { value1,value2,value3 }, new MsgSettings());
	}

	private static BaseResponse testAdd(IIDManageServiceChannel channel) throws Exception {
		IdentifierValue[] values = new IdentifierValue[2];
		values[0] = makePublicKeyValue();
		values[1] = makeCertValue(channel);
		// values[0] = makeSiteInfoPrefixValue();
		// values[0] = makeSignatureValue(channel);
		// values[0] = makeAdminValue();
		// values[0] = new IdentifierValue(66, "URL", "www.666.com");
		// values[1] = new IdentifierValue(7, "email", "www.777.com");
		// values[1] = makePublicKeyValue();
		return channel.addIdentifierValues("88.1234", values, new MsgSettings());
	}

	private static BaseResponse testEdit(IIDManageServiceChannel channel) throws IdentifierException {
		IdentifierValue[] values = new IdentifierValue[2];
		values[0] = new IdentifierValue(6, "email", "www.666e.com");
		values[1] = new IdentifierValue(7, "url", "www.777e.com");
		return channel.modifyIdentifierValues("88.1000.2/mm", values, new MsgSettings());
	}

	private static BaseResponse testRemove(IIDManageServiceChannel channel) throws IdentifierException {
		IdentifierValue[] values = new IdentifierValue[2];
		values[0] = new IdentifierValue(6, "email", "www.666e.com");
		values[1] = new IdentifierValue(7, "url", "www.777e.com");
		int[] arr = { 6, 7 };
		return channel.removeIdentifierValues("88.1000.2/mm", arr, null);
	}

	private static BaseResponse testLookup(IIDManageServiceChannel channel) throws IdentifierException {
		String identifier = "88.1000.2/cupA";
		// String identifier = "88.888.888/mm";
		int[] arr = null;
		String[] types = null;
		return channel.lookupIdentifier(identifier, arr, types, null);
	}

	private static BaseResponse testGetSiteInfo(IIDManageServiceChannel channel) throws IdentifierException {
		MsgSettings settings = new MsgSettings();
		BaseResponse response = channel.getServerSiteInfo(settings);
		if (response instanceof SiteResponse) {
			SiteInfo si = ((SiteResponse) response).getSiteInfo();
			System.out.println("siteInfo:" + si);
		}
		return response;
	}

	private static IdentifierValue makeAdminValue() throws IdentifierException {
		IdentifierValue value = new IdentifierValue();
		AdminInfo admin = new AdminInfo();
		admin.admId = Util.encodeString("88.1000.2/cupA");
		admin.admIdIndex = 302;
		admin.initPermissions(true, true, true, true, true, true, true, true, true, true, true, true);
		IdentifierValueUtil.makeIdentifierValueOfAdminInfo(value, admin, 10);
		return value;
	}

	private static IdentifierValue makeSiteInfoValue() throws IdentifierException, IOException {
		IdentifierValue iv = new IdentifierValue();
		int index = 20;
		// items[]
		IdisCommunicationItems[] items = new IdisCommunicationItems[2];
		items[0] = new IdisCommunicationItems(IdisCommunicationItems.ST_ADMIN_AND_QUERY,
				IdisCommunicationItems.TS_IDF_TCP, 1304);
		items[1] = new IdisCommunicationItems(IdisCommunicationItems.ST_ADMIN_AND_QUERY,
				IdisCommunicationItems.TS_IDF_UDP, 1304);
		// server
		ServerInfo ser1 = new ServerInfo();
		ser1.communicationItems = items;
		ser1.ipBytes = Util.convertIPStr2Bytes("192.168.150.13");
		ser1.publicKey = Util.getBytesFromFile("D:/temp/svr_1/admpub.bin");
		ser1.serverId = 1;
		// servers
		ServerInfo[] servArr = new ServerInfo[] { ser1 };
		// siteinfo
		SiteInfo si = new SiteInfo();
		si.servers = servArr;
		si.attributes = null;

		IdentifierValueUtil.makeIdentifierValueOfSiteInfo(iv, si, index);
		return iv;
	}

	private static IdentifierValue makeSiteInfoPrefixValue() throws IdentifierException {
		IdentifierValue iv = new IdentifierValue();
		int index = 25;
		// items[]
		IdisCommunicationItems[] items = new IdisCommunicationItems[2];
		items[0] = new IdisCommunicationItems(IdisCommunicationItems.ST_ADMIN_AND_QUERY,
				IdisCommunicationItems.TS_IDF_TCP, 2641);
		items[1] = new IdisCommunicationItems(IdisCommunicationItems.ST_ADMIN_AND_QUERY,
				IdisCommunicationItems.TS_IDF_UDP, 2641);
		// server
		ServerInfo ser1 = new ServerInfo();
		ser1.communicationItems = items;
		ser1.ipBytes = Util.convertIPStr2Bytes("192.168.150.25");
		ser1.publicKey = null;
		ser1.serverId = 1;
		// servers
		ServerInfo[] servArr = new ServerInfo[] { ser1 };
		// siteinfo
		SiteInfo si = new SiteInfo();
		si.servers = servArr;
		si.attributes = null;
		IdentifierValueUtil.makeIdentifierValueOfSiteInfoPrefix(iv, si, index);
		return iv;
	}

	private static IdentifierValue makeVListValue() throws IdentifierException {
		IdentifierValue iv = new IdentifierValue();
		int index = 30;
		ValueReference[] vr = new ValueReference[2];
		vr[0] = new ValueReference("88.1000.2/mm", 1);
		vr[1] = new ValueReference("88.1000.2/cup", 2);
		IdentifierValueUtil.makeIdentifierValueOfVList(iv, vr, index);
		return iv;
	}

	private static IdentifierValue makePublicKeyValue() throws IdentifierException {
		IdentifierValue iv = new IdentifierValue();
		int index = 300;
		IdentifierValueUtil.makeIdentifierValueOfPublicKey(iv,
				"D:/工作/客户端-无登录/hsclient_new_lhs_resolver/bin/rsa_pub.pem", index);
		return iv;
	}

	private static IdentifierValue makeSignatureValue(IIDManageServiceChannel channel) throws Exception {
		IdentifierValue iv = new IdentifierValue();
		int index = 400;
		String pubPath = "D:/工作/客户端-无登录/hsclient_new_lhs_resolver/bin/rsa_pub.pem";
		PublicKey pubKey = Util.getPublicKeyFromFile(pubPath);
		String prvPath = "D:/工作/客户端-无登录/hsclient_new_lhs_resolver/bin/rsa_pri.bin";
		PrivateKey prvKey = Util.getPrivateKeyFromFile(prvPath, null);

		IdentifierValue[] values = new IdentifierValue[1];
		BaseResponse response = channel.lookupIdentifier("88.1234.1234", null, null, null);

		if (response instanceof ResolutionResponse) {
			values = ((ResolutionResponse) response).getAllIDValues();
		}

		SignatureInfo signInfo = new SignatureInfo(prvKey, pubKey, values, "300:88.1234", "88.1234.1234",
				System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), "SHA-256");
		IdentifierValueUtil.makeIdentifierValueOfSignature(iv, index, signInfo);
		return iv;
	}

	private static IdentifierValue makeCertValue(IIDManageServiceChannel channel) throws Exception {
		String pubPath = "D:/工作/客户端-无登录/hsclient_new_lhs_resolver/bin/rsa_pub.pem";
		PublicKey pubKey = Util.getPublicKeyFromFile(pubPath);
		String prvPath = "D:/工作/工作文档集/86的公私钥/86的公私钥/发证书使用的公私钥/rsa_private_pkcs8.bin";
		PrivateKey prvKey = Util.getPrivateKeyFromFile(prvPath, null);

		List<Permission> perms = new ArrayList<>();
		perms.add(new Permission(null, "everything"));

		IdentifierValue iv = new IdentifierValue();
		int index = 401;

		long oneYearInSeconds = 365L * 24L * 60L * 60L;
		long expiration = System.currentTimeMillis() / 1000L + (oneYearInSeconds * 2);

		SignatureInfo signInfo = new SignatureInfo(prvKey, pubKey, perms, "100:88", "300:88.1234", expiration,
				System.currentTimeMillis(), System.currentTimeMillis() - 600);
		IdentifierValueUtil.makeIdentifierValueOfCertification(iv, index, signInfo);
		return iv;
	}

}
