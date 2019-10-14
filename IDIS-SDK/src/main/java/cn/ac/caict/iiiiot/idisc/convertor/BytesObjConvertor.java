package cn.ac.caict.iiiiot.idisc.convertor;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *© COPYRIGHT 2019 Corporation for Institute of Industrial Internet & Internet of Things (IIIIT);
 *                      All rights reserved. 
 * http://www.caict.ac.cn  
 * https://www.citln.cn/
 */
import cn.ac.caict.iiiiot.idisc.core.Attribute;
import cn.ac.caict.iiiiot.idisc.core.IdentifierException;
import cn.ac.caict.iiiiot.idisc.core.IdisCommunicationItems;
import cn.ac.caict.iiiiot.idisc.core.ServerInfo;
import cn.ac.caict.iiiiot.idisc.core.SiteInfo;
import cn.ac.caict.iiiiot.idisc.data.AdminInfo;
import cn.ac.caict.iiiiot.idisc.data.ValueReference;
import cn.ac.caict.iiiiot.idisc.utils.Common;
import cn.ac.caict.iiiiot.idisc.utils.ExceptionCommon;

public class BytesObjConvertor extends BaseConvertor {

	public static final AdminInfo bytesConvertToAdminInfo(byte[] data) throws IdentifierException {
		int offset = 0;
		int perm = 0;
		perm = read2Bytes(data, offset);
		offset += Common.TWO_SIZE;

		byte[] adminId = readByteArray(data, offset);
		offset = offset + Common.FOUR_SIZE + adminId.length;

		int index = read4Bytes(data, offset);
		offset += Common.FOUR_SIZE;

		return new AdminInfo(adminId, index, perm);
	}

	public static final SiteInfo bytesCovertToSiteInfo(byte[] data) throws IdentifierException {
		SiteInfo siteInfo = new SiteInfo();
		int offset = 0;
		siteInfo.dataFormatVersion = read2Bytes(data, offset);
		offset += Common.TWO_SIZE;
		siteInfo.majorProtocolVersion = data[offset];
		offset += 1;
		siteInfo.minorProtocolVersion = data[offset];
		offset += 1;
		siteInfo.serialNumber = read2Bytes(data, offset);
		offset += Common.TWO_SIZE;

		siteInfo.isPrimarySite = (data[offset] & SiteInfo.PRIMARY_SITE) != 0;
		siteInfo.isMultiPrimarySite = (data[offset] & SiteInfo.MULTI_PRIMARY) != 0;
		offset += 1;

		siteInfo.hashOption = data[offset];
		offset += 1;

		siteInfo.hashFilter = readByteArray(data, offset);
		offset += Common.FOUR_SIZE + siteInfo.hashFilter.length;

		int arrtNum = read4Bytes(data, offset);
		offset += Common.FOUR_SIZE;

		if (arrtNum == 0) {
			siteInfo.attributes = null;
		} else {
			siteInfo.attributes = new Attribute[arrtNum];
			for (int i = 0; i < arrtNum; i++) {
				siteInfo.attributes[i] = new Attribute();
				siteInfo.attributes[i].name = readByteArray(data, offset);
				offset += Common.FOUR_SIZE + siteInfo.attributes[i].name.length;
				siteInfo.attributes[i].value = readByteArray(data, offset);
				offset += Common.FOUR_SIZE + siteInfo.attributes[i].value.length;
			}
		}

		int servNum = read4Bytes(data, offset);
		if (servNum < 0 || servNum > Common.MAX_ARRAY_SIZE)
			throw new IdentifierException(ExceptionCommon.EXCEPTIONCODE_MESSAGE_FORMAT_ERROR, "消息尺寸越界");
		if (servNum == 0) {
			siteInfo.servers = null;
		} else {
			siteInfo.servers = new ServerInfo[servNum];
			for (int j = 0; j < servNum; j++) {
				siteInfo.servers[j] = new ServerInfo();
				siteInfo.servers[j].serverId = read4Bytes(data, offset);
				offset += Common.FOUR_SIZE;
				siteInfo.servers[j].ipBytes = new byte[Common.IP_ADDRESS_SIZE_SIXTEEN];
				System.arraycopy(data, offset, siteInfo.servers[j].ipBytes, 0, Common.IP_ADDRESS_SIZE_SIXTEEN);
				offset += Common.IP_ADDRESS_SIZE_SIXTEEN;
				siteInfo.servers[j].publicKey = readByteArray(data, offset);
				offset += Common.FOUR_SIZE + siteInfo.servers[j].publicKey.length;

				int itemNum = read4Bytes(data, offset);
				offset += Common.FOUR_SIZE;

				if (itemNum < 0 || itemNum > Common.MAX_ARRAY_SIZE)
					throw new IdentifierException(ExceptionCommon.EXCEPTIONCODE_MESSAGE_FORMAT_ERROR, "消息尺寸越界");
				if (itemNum == 0) {
					siteInfo.servers[j].communicationItems = null;
				} else {
					siteInfo.servers[j].communicationItems = new IdisCommunicationItems[itemNum];
					for (int k = 0; k < itemNum; k++) {
						IdisCommunicationItems items = new IdisCommunicationItems();
						siteInfo.servers[j].communicationItems[k] = items;
						items.type = data[offset];
						offset += 1;
						items.protocol = data[offset];
						offset += 1;
						items.port = read4Bytes(data, offset);
						offset += Common.FOUR_SIZE;
					}
				}
				if (offset < data.length)
					throw new IdentifierException(ExceptionCommon.EXCEPTIONCODE_MESSAGE_FORMAT_ERROR,
							"按字节读取数据后不应该还有剩余字节");
			}
		}
		return siteInfo;
	}

	public static ValueReference[] bytesCovertToVList(byte[] data) throws IdentifierException {
		int offset = 0;
		int valueNum = read4Bytes(data, offset);
		offset += Common.FOUR_SIZE;
		if (valueNum < 0 || valueNum > Common.MAX_ARRAY_SIZE)
			throw new IdentifierException(ExceptionCommon.EXCEPTIONCODE_MESSAGE_FORMAT_ERROR, "消息尺寸越界");
		if (valueNum == 0)
			return null;
		ValueReference[] vRef = new ValueReference[valueNum];
		for (int i = 0; i < valueNum; i++) {
			vRef[i] = new ValueReference();
			vRef[i].identifier = readByteArray(data, offset);
			offset += Common.FOUR_SIZE + vRef[i].identifier.length;
			vRef[i].index = read4Bytes(data, offset);
			offset += Common.FOUR_SIZE;
		}
		if (offset < data.length)
			throw new IdentifierException(ExceptionCommon.EXCEPTIONCODE_MESSAGE_FORMAT_ERROR, "按字节读取数据后不应该还有剩余字节");
		return vRef;
	}
}
