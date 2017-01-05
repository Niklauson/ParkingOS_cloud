package com.zld.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zld.CustomDefind;
import com.zld.pojo.WorkRecord;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
@Repository
public class CommonMethods {
	
	
	private Logger logger = Logger.getLogger(CommonMethods.class);
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	
	/**
	 * ��ҳ����̨��ת������ҳ�棬����ʱ��Ҫ��ҳ����Ȩ���
	 * @return
	 */
	public void setIndexAuthId(HttpServletRequest request){
		try {
			request.setAttribute("from", RequestUtil.processParams(request, "from"));
			List<Map<String, Object>> authList = (List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			if(authList != null){
				for(Map<String, Object> map : authList){
					if(map.get("url") != null){
						String url = (String)map.get("url");
						if(url.contains("cityindex.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
						if(url.contains("citysensor.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
						if(url.contains("citytransmitter.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
						if(url.contains("cityinduce.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ������
	 * @param key
	 * @return
	 */
	public String getLock(Object key){
		String lock = null;
		try {
			String className = Thread.currentThread().getStackTrace()[2].getClassName();//��ȡ��ǰ��������һ�������ߵ�����
			String methodName = Thread.currentThread() .getStackTrace()[2].getMethodName();//��ȡ��ǰ��������һ�������ߵķ�����
			lock = className + "-" + methodName + "-" + key;
			return lock;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lock;
	}
	
	/**
	 * ��ȡ�շ�Ա��ǰǩ���Ĺ�����¼
	 * @param parkUserId
	 * @return
	 */
	public WorkRecord getWorkRecord(Long parkUserId){
		try {
			WorkRecord workRecord = pgOnlyReadService.getPOJO("select * from parkuser_work_record_tb where " +
					" uid=? and state=? order by id desc limit ? ", 
					new Object[]{parkUserId, 0,  1}, WorkRecord.class);
			return workRecord;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * ��ѯ�����շ�Ա���������
	 * @param idList
	 * @param beginTime
	 * @param endTime
	 * @param type
	 * @return
	 */
	public List<Map<String, Object>> getIncomeByTimeAnly(List<Object> idList, Long beginTime, Long endTime, int type){
		try {
			if(idList != null && !idList.isEmpty()){
				String preParam = "";
				for(Object object : idList){
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Object> params = new ArrayList<Object>();
				params.add(beginTime);
				params.add(endTime);
				params.addAll(idList);
				params.add(type);
				String sql = "select create_time,sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card,sum(charge_card_cash) charge_card_cash,sum(return_card_count) return_card_count," +
						"sum(return_card_fee) return_card_fee,sum(act_card_count) act_card_count,sum(act_card_fee) act_card_fee," +
						"sum(reg_card_count) reg_card_count,sum(reg_card_fee) reg_card_fee,sum(bind_card_count) bind_card_count from " +
						"parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+preParam+") and type=? group by create_time ";
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * ��ѯ�����շ�Ա���������
	 * @param idList
	 * @param beginTime
	 * @param endTime
	 * @param type
	 * @return
	 */
	public Map<String, Object> sumIncomeAnly(List<Object> idList, Long beginTime, Long endTime, int type){
		try {
			if(idList != null && !idList.isEmpty()){
				String preParam = "";
				for(Object object : idList){
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Object> params = new ArrayList<Object>();
				params.add(beginTime);
				params.add(endTime);
				params.addAll(idList);
				params.add(type);
				String sql = "select sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card,sum(charge_card_cash) charge_card_cash,sum(return_card_count) return_card_count," +
						"sum(return_card_fee) return_card_fee,sum(act_card_count) act_card_count,sum(act_card_fee) act_card_fee," +
						"sum(reg_card_count) reg_card_count,sum(reg_card_fee) reg_card_fee,sum(bind_card_count) bind_card_count from " +
						"parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+preParam+") and type=? ";
				Map<String, Object> infoMap = pgOnlyReadService.getMap(sql, params);
				return infoMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * ��ѯÿ���շ�Ա���շ����
	 * @param idList
	 * @param beginTime
	 * @param endTime
	 * @param type
	 * @return
	 */
	public List<Map<String, Object>> getIncomeAnly(List<Object> idList, Long beginTime, Long endTime, int type){
		try {
			if(idList != null && !idList.isEmpty()){
				List<Object> paramsList = new ArrayList<Object>();
				paramsList.add(beginTime);
				paramsList.add(endTime);
				paramsList.addAll(idList);
				paramsList.add(type);
				String param = "";
				for(Object object : idList){
					if(param.equals("")){
						param = "?";
					}else{
						param += ",?";
					}
				}
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap("select uin as id,sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card,sum(charge_card_cash) charge_card_cash,sum(return_card_count) return_card_count," +
						"sum(return_card_fee) return_card_fee,sum(act_card_count) act_card_count,sum(act_card_fee) act_card_fee," +
						"sum(reg_card_count) reg_card_count,sum(reg_card_fee) reg_card_fee,sum(bind_card_count) bind_card_count from " +
						"parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+param+") and type=? group by uin", paramsList);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Map<String, Object>> getIncome(Long startTime, Long endTime, List<Object> comidList, List<Object> uinList, Map<String, Object> otherMap){
		try {
			List<Object> params = new ArrayList<Object>();
			String preParams = "";
			String sql = "";
			String groupSql = "";
			if(comidList != null && !comidList.isEmpty()){
				for(Object o : comidList){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql = " o.comid in ("+preParams+")";
				groupSql = ",o.comid ";
				params.addAll(comidList);
			}else if(uinList != null && !uinList.isEmpty()){
				for(Object o : uinList){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql = " a.uin in ("+preParams+")";
				groupSql = ",a.uin";
				params.addAll(uinList);
			}
			List<Object> params1 = new ArrayList<Object>();
			params1.addAll(params);
			params1.add(0);//ͣ���ѣ���Ԥ��
			params1.add(1);//Ԥ��ͣ����
			params1.add(2);//Ԥ���˿Ԥ�����
			params1.add(3);//Ԥ�����ɣ�Ԥ�����㣩
			params1.add(4);//׷��ͣ����
			SqlInfo sqlInfo1 = new SqlInfo(sql + " and a.target in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list1 = anlysisMoney(1, startTime, endTime, new String[]{"a.target" + groupSql}, sqlInfo1, null);
			params1.clear();
			params1.addAll(params);
			params1.add(4);//������ͣ���ѣ���Ԥ�������ߴ����շ�Ա
			params1.add(5);//׷��ͣ����
			params1.add(6);//����Ԥ��ͣ����
			params1.add(7);//Ԥ���˿Ԥ�����
			params1.add(8);//Ԥ�����ɣ�Ԥ�����㣩
			SqlInfo sqlInfo2 = new SqlInfo(sql + " and a.target in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list2 = anlysisMoney(2, startTime, endTime, new String[]{"a.target" + groupSql}, sqlInfo2, null);
			params1.clear();
			params1.addAll(params);
			params1.add(0);//ͣ���ѣ���Ԥ����
			params1.add(7);//׷��ͣ����
			params1.add(8);//����Ԥ��ͣ����
			params1.add(9);//Ԥ���˿Ԥ�����
			params1.add(10);//Ԥ�����ɣ�Ԥ�����㣩
			String sql2 = sql;
			String groupSql2 = groupSql;
			if(sql.contains("a.uin")){
				sql2 = sql.replace("a.uin", "a.uid");
			}
			if(groupSql.contains("a.uin")){
				groupSql2 = groupSql.replace("a.uin", "a.uid");
			}
			SqlInfo sqlInfo3 = new SqlInfo(sql2 + " and a.source in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list3 = anlysisMoney(3, startTime, endTime, new String[]{"a.source" + groupSql2}, sqlInfo3, null);
			params1.clear();
			params1.addAll(params);
			params1.add(0);//ͣ���ѣ���Ԥ����
			params1.add(2);//׷��ͣ����
			params1.add(3);//Ԥ��ͣ����
			params1.add(4);//Ԥ���˿Ԥ����
			params1.add(5);//Ԥ�����ɣ�Ԥ�����㣩
			SqlInfo sqlInfo4 = new SqlInfo(sql2 + " and a.source in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list4 = anlysisMoney(4, startTime, endTime, new String[]{"a.source" + groupSql2}, sqlInfo4, null);
			params1.clear();
			params1.addAll(params);
			params1.add(0);//ͣ���ѣ���Ԥ����
			params1.add(2);//׷��ͣ����
			params1.add(3);//Ԥ��ͣ����
			params1.add(4);//Ԥ���˿Ԥ����
			params1.add(5);//Ԥ�����ɣ�Ԥ�����㣩
			SqlInfo sqlInfo5 = new SqlInfo(sql2 + " and a.source in (?,?,?,?,?)", params1);
			List<Map<String, Object>> list5 = anlysisMoney(5, startTime, endTime, new String[]{"a.source" + groupSql2}, sqlInfo5, null);
			params1.clear();
			params1.addAll(params);
			String sql3 = sql;
			String groupSql3 = groupSql;
			if(sql.contains("a.uin")){
				sql3 = sql.replace("a.uin", "uid");
			}
			if(sql.contains("o.comid")){
				sql3 = sql.replace("o.comid", "comid");
			}
			if(groupSql.contains("a.uin")){
				groupSql3 = groupSql.replace("a.uin", "uid");
			}
			if(groupSql.contains("o.comid")){
				groupSql3 = groupSql.replace("o.comid", "comid");
			}
			if(groupSql3.contains(",")){
				groupSql3 = groupSql3.substring(1);
			}
			SqlInfo sqlInfo6 = new SqlInfo(sql3, params1);
			List<Map<String, Object>> list6 = anlysisMoney(6, startTime, endTime, new String[]{groupSql3}, sqlInfo6, null);
			String sql4 = sql;
			String groupSql4 = groupSql;
			if(sql.contains("a.uin")){
				sql4 = sql.replace("a.uin", "out_uid");
			}
			if(sql.contains("o.comid")){
				sql4 = sql.replace("o.comid", "comid");
			}
			if(groupSql.contains("a.uin")){
				groupSql4 = groupSql.replace("a.uin", "out_uid");
			}
			if(groupSql.contains("o.comid")){
				groupSql4 = groupSql.replace("o.comid", "comid");
			}
			if(groupSql4.contains(",")){
				groupSql4 = groupSql4.substring(1);
			}
			SqlInfo sqlInfo7 = new SqlInfo(sql4, params1);
			List<Map<String, Object>> list7 = anlysisMoney(7, startTime, endTime, new String[]{groupSql4}, sqlInfo7, null);
			String sql5 = sql;
			String groupSql5 = groupSql;
			if(sql.contains("a.uin")){
				sql5 = sql.replace("a.uin", "o.uid");
			}
			if(groupSql.contains("a.uin")){
				groupSql5 = groupSql.replace("a.uin", "o.uid");
			}
			if(groupSql5.contains(",")){
				groupSql5 = groupSql5.substring(1);
			}
			SqlInfo sqlInfo8 = new SqlInfo(sql5, params1);
			List<Map<String, Object>> list8 = anlysisMoney(8, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list9 = anlysisMoney(9, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list10 = anlysisMoney(10, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list11 = anlysisMoney(11, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list12 = anlysisMoney(12, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			List<Map<String, Object>> list14 = anlysisMoney(14, startTime, endTime, new String[]{groupSql5}, sqlInfo8, null);
			params1.clear();
			params1.addAll(params);
			params1.add(4);//charge_type -- ��ֵ��ʽ��4��Ԥ֧���˿�
			params1.add(0);//consume_type --���ѷ�ʽ 0��֧��ͣ���ѣ���Ԥ����
			params1.add(1);//consume_type --���ѷ�ʽ 1��Ԥ��ͣ����
			params1.add(2);//consume_type --���ѷ�ʽ 2������ͣ����
			params1.add(3);//consume_type --���ѷ�ʽ3��׷��ͣ����
			SqlInfo sqlInfo9 = new SqlInfo(sql2 + " and (a.charge_type in (?) or a.consume_type in (?,?,?,?))", params1);
			List<Map<String, Object>> list13 = anlysisMoney(13, startTime, endTime, 
					new String[]{"a.charge_type,a.consume_type" + groupSql2}, sqlInfo9, null);
			
			List<Map<String, Object>> infoList = new ArrayList<Map<String,Object>>();
			List<Object> idList = new ArrayList<Object>();
			mergeIncome(1, list1, infoList, idList);
			mergeIncome(2, list2, infoList, idList);
			mergeIncome(3, list3, infoList, idList);
			mergeIncome(4, list4, infoList, idList);
			mergeIncome(5, list5, infoList, idList);
			mergeIncome(6, list6, infoList, idList);
			mergeIncome(7, list7, infoList, idList);
			mergeIncome(8, list8, infoList, idList);
			mergeIncome(9, list9, infoList, idList);
			mergeIncome(10, list10, infoList, idList);
			mergeIncome(11, list11, infoList, idList);
			mergeIncome(12, list12, infoList, idList);
			mergeIncome(13, list13, infoList, idList);
			mergeIncome(14, list14, infoList, idList);
			return infoList;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private List<Map<String, Object>> mergeIncome(int type, List<Map<String, Object>> list, List<Map<String, Object>> infoList, List<Object> idList){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = null;
					Integer target = null;
					Double summoney = 0d;
					if(map.get("summoney") != null){
						summoney = Double.valueOf(map.get("summoney") + "");
					}
					if(map.get("comid") != null){
						id = (Long)map.get("comid");
					}else if(map.get("uin") != null){
						id = (Long)map.get("uin");
					}else if(map.get("uid") != null){
						id = (Long)map.get("uid");
					}else if(map.get("out_uid") != null){
						id = (Long)map.get("out_uid");
					}
					if(map.get("target") != null){
						target = (Integer)map.get("target");
					}else if(map.get("source") != null){
						target = (Integer)map.get("source");
					}
					if(idList.contains(id)){
						for(Map<String, Object> infoMap : infoList){
							Long infoId = (Long)infoMap.get("id");
							if(id.intValue() == infoId.intValue()){
								Double prepay_cash = Double.valueOf(infoMap.get("prepay_cash") + "");//�ֽ�Ԥ֧��
								Double add_cash = Double.valueOf(infoMap.get("add_cash") + "");//�ֽ𲹽�
								Double refund_cash = Double.valueOf(infoMap.get("refund_cash") + "");//�ֽ��˿�
								Double pursue_cash = Double.valueOf(infoMap.get("pursue_cash") + "");//�ֽ�׷��
								Double pfee_cash = Double.valueOf(infoMap.get("pfee_cash") + "");//�ֽ�ͣ���ѣ���Ԥ����
								Double prepay_epay = Double.valueOf(infoMap.get("prepay_epay") + "");//����Ԥ֧��
								Double add_epay = Double.valueOf(infoMap.get("add_epay") + "");//���Ӳ���
								Double refund_epay = Double.valueOf(infoMap.get("refund_epay") + "");//�����˿�
								Double pursue_epay = Double.valueOf(infoMap.get("pursue_epay") + "");//����׷��
								Double pfee_epay = Double.valueOf(infoMap.get("pfee_epay") + "");//����ͣ���ѣ���Ԥ����
								Double escape = Double.valueOf(infoMap.get("escape") + "");//�ӵ�δ׷�ɵ�ͣ����
								Double prepay_escape = Double.valueOf(infoMap.get("prepay_escape") + "");//�ӵ�δ׷�ɵĶ�����Ԥ�ɵĽ��
								Double sensor_fee = Double.valueOf(infoMap.get("sensor_fee") + "");//������ͣ����
								Double prepay_card = Double.valueOf(infoMap.get("prepay_card") + "");//ˢ��Ԥ֧��
								Double add_card = Double.valueOf(infoMap.get("add_card") + "");//ˢ������
								Double refund_card = Double.valueOf(infoMap.get("refund_card") + "");//ˢ���˿�
								Double pursue_card = Double.valueOf(infoMap.get("pursue_card") + "");//ˢ��׷��
								Double pfee_card = Double.valueOf(infoMap.get("pfee_card") + "");//ˢ��ͣ���ѣ���Ԥ����
								if(type == 1){
									if(target == 0){//�ֽ�ͣ���ѣ���Ԥ����
										pfee_cash += summoney;
									}else if(target == 1){//Ԥ��ͣ����
										prepay_cash += summoney;
									}else if(target == 2){//Ԥ���˿Ԥ�����
										refund_cash += summoney;
									}else if(target == 3){//Ԥ�����ɣ�Ԥ�����㣩
										add_cash += summoney; 
									}else if(target == 4){//׷��ͣ����
										pursue_cash += summoney;
									}
								}else if(type == 2){
									if(target == 4){//������ͣ���ѣ���Ԥ�������ߴ����շ�Ա
										pfee_epay += summoney;
									}else if(target == 5){//׷��ͣ����
										pursue_epay += summoney;
									}else if(target == 6){//Ԥ��ͣ����
										prepay_epay += summoney;
									}else if(target == 7){//Ԥ���˿Ԥ�����
										refund_epay += summoney;
									}else if(target == 8){//Ԥ�����ɣ�Ԥ�����㣩
										add_epay += summoney; 
									}
								}else if(type == 3){
									if(target == 0){//ͣ���ѣ���Ԥ����
										pfee_epay += summoney;
									}else if(target == 7){//׷��ͣ����
										pursue_epay += summoney;
									}else if(target == 8){//Ԥ��ͣ����
										prepay_epay += summoney;
									}else if(target == 9){//Ԥ���˿Ԥ�����
										refund_epay += summoney;
									}else if(target == 10){//Ԥ�����ɣ�Ԥ�����㣩
										add_epay += summoney; 
									}
								}else if(type == 4){
									if(target == 0){//ͣ���ѣ���Ԥ����
										pfee_epay += summoney;
									}else if(target == 2){//׷��ͣ����
										pursue_epay += summoney;
									}else if(target == 3){//Ԥ��ͣ����
										prepay_epay += summoney;
									}else if(target == 4){//Ԥ���˿Ԥ�����
										refund_epay += summoney;
									}else if(target == 5){//Ԥ�����ɣ�Ԥ�����㣩
										add_epay += summoney; 
									}
								}else if(type == 5){
									if(target == 0){//ͣ���ѣ���Ԥ����
										pfee_epay += summoney;
									}else if(target == 2){//׷��ͣ����
										pursue_epay += summoney;
									}else if(target == 3){//Ԥ��ͣ����
										prepay_epay += summoney;
									}else if(target == 4){//Ԥ���˿Ԥ�����
										refund_epay += summoney;
									}else if(target == 5){//Ԥ�����ɣ�Ԥ�����㣩
										add_epay += summoney; 
									}
								}else if(type == 6){
									escape += summoney;
								}else if(type == 7){
									sensor_fee += summoney;
								}else if(type == 8 
										|| type == 9 
										|| type == 10 
										|| type == 11 
										|| type == 12 
										|| type == 14){
									prepay_escape += summoney;
								}else if(type == 13){
									Integer charge_type = (Integer)map.get("charge_type");
									Integer consume_type = (Integer)map.get("consume_type");
									if(charge_type == 4){//4��Ԥ֧���˿�
										refund_card += summoney;
									}else if(consume_type == 0){//0��֧��ͣ���ѣ���Ԥ����
										pfee_card += summoney;
									}else if(consume_type == 1){//1��Ԥ��ͣ����
										prepay_card += summoney;
									}else if(consume_type == 2){//2������ͣ����
										add_card += summoney;
									}else if(consume_type == 3){//3��׷��ͣ����
										pursue_card += summoney;
									}
								}
								infoMap.put("prepay_cash", prepay_cash);
								infoMap.put("add_cash", add_cash);
								infoMap.put("refund_cash", refund_cash);
								infoMap.put("pursue_cash", pursue_cash);
								infoMap.put("pfee_cash", pfee_cash);
								infoMap.put("prepay_epay", prepay_epay);
								infoMap.put("add_epay", add_epay);
								infoMap.put("refund_epay", refund_epay);
								infoMap.put("pursue_epay", pursue_epay);
								infoMap.put("pfee_epay", pfee_epay);
								infoMap.put("escape", escape);
								infoMap.put("prepay_escape", prepay_escape);
								infoMap.put("sensor_fee", sensor_fee);
								infoMap.put("prepay_card", prepay_card);
								infoMap.put("add_card", add_card);
								infoMap.put("refund_card", refund_card);
								infoMap.put("pursue_card", pursue_card);
								infoMap.put("pfee_card", pfee_card);
							}
						}
					}else{
						idList.add(id);
						Double prepay_cash = 0d;
						Double add_cash = 0d;
						Double refund_cash = 0d;
						Double pursue_cash = 0d;
						Double pfee_cash = 0d;
						Double prepay_epay = 0d;
						Double add_epay = 0d;
						Double refund_epay = 0d;
						Double pfee_epay = 0d;
						Double pursue_epay = 0d;
						Double escape = 0d;
						Double prepay_escape = 0d;
						Double sensor_fee = 0d;
						Double prepay_card = 0d;
						Double add_card = 0d;
						Double refund_card = 0d;
						Double pursue_card = 0d;
						Double pfee_card = 0d;
						if(type == 1){
							if(target == 0){//�ֽ�ͣ���ѣ���Ԥ����
								pfee_cash += summoney;
							}else if(target == 1){//Ԥ��ͣ����
								prepay_cash += summoney;
							}else if(target == 2){//Ԥ���˿Ԥ�����
								refund_cash += summoney;
							}else if(target == 3){//Ԥ�����ɣ�Ԥ�����㣩
								add_cash += summoney; 
							}else if(target == 4){//׷��ͣ����
								pursue_cash += summoney;
							}
						}else if(type == 2){
							if(target == 4){//������ͣ���ѣ���Ԥ�������ߴ����շ�Ա
								pfee_epay += summoney;
							}else if(target == 5){//׷��ͣ����
								pursue_epay += summoney;
							}else if(target == 6){//Ԥ��ͣ����
								prepay_epay += summoney;
							}else if(target == 7){//Ԥ���˿Ԥ�����
								refund_epay += summoney;
							}else if(target == 8){//Ԥ�����ɣ�Ԥ�����㣩
								add_epay += summoney; 
							}
						}else if(type == 3){
							if(target == 0){//ͣ���ѣ���Ԥ����
								pfee_epay += summoney;
							}else if(target == 7){//׷��ͣ����
								pursue_epay += summoney;
							}else if(target == 8){//Ԥ��ͣ����
								prepay_epay += summoney;
							}else if(target == 9){//Ԥ���˿Ԥ�����
								refund_epay += summoney;
							}else if(target == 10){//Ԥ�����ɣ�Ԥ�����㣩
								add_epay += summoney; 
							}
						}else if(type == 4){
							if(target == 0){//ͣ���ѣ���Ԥ����
								pfee_epay += summoney;
							}else if(target == 2){//׷��ͣ����
								pursue_epay += summoney;
							}else if(target == 3){//Ԥ��ͣ����
								prepay_epay += summoney;
							}else if(target == 4){//Ԥ���˿Ԥ�����
								refund_epay += summoney;
							}else if(target == 5){//Ԥ�����ɣ�Ԥ�����㣩
								add_epay += summoney; 
							}
						}else if(type == 5){
							if(target == 0){//ͣ���ѣ���Ԥ����
								pfee_epay += summoney;
							}else if(target == 2){//׷��ͣ����
								pursue_epay += summoney;
							}else if(target == 3){//Ԥ��ͣ����
								prepay_epay += summoney;
							}else if(target == 4){//Ԥ���˿Ԥ�����
								refund_epay += summoney;
							}else if(target == 5){//Ԥ�����ɣ�Ԥ�����㣩
								add_epay += summoney; 
							}
						}else if(type == 6){
							escape += summoney;
						}else if(type == 7){
							sensor_fee += summoney;
						}else if(type == 8 
								|| type == 9 
								|| type == 10 
								|| type == 11 
								|| type == 12 
								|| type ==14){
							prepay_escape += summoney;
						}else if(type == 13){
							Integer charge_type = (Integer)map.get("charge_type");
							Integer consume_type = (Integer)map.get("consume_type");
							if(charge_type == 4){//4��Ԥ֧���˿�
								refund_card += summoney;
							}else if(consume_type == 0){//0��֧��ͣ���ѣ���Ԥ����
								pfee_card += summoney;
							}else if(consume_type == 1){//1��Ԥ��ͣ����
								prepay_card += summoney;
							}else if(consume_type == 2){//2������ͣ����
								add_card += summoney;
							}else if(consume_type == 3){//3��׷��ͣ����
								pursue_card += summoney;
							}
						}
						Map<String, Object> infoMap = new HashMap<String, Object>();
						infoMap.put("prepay_cash", prepay_cash);
						infoMap.put("add_cash", add_cash);
						infoMap.put("refund_cash", refund_cash);
						infoMap.put("pursue_cash", pursue_cash);
						infoMap.put("pfee_cash", pfee_cash);
						infoMap.put("prepay_epay", prepay_epay);
						infoMap.put("add_epay", add_epay);
						infoMap.put("refund_epay", refund_epay);
						infoMap.put("pfee_epay", pfee_epay);
						infoMap.put("pursue_epay", pursue_epay);
						infoMap.put("escape", escape);
						infoMap.put("prepay_escape", prepay_escape);
						infoMap.put("sensor_fee", sensor_fee);
						infoMap.put("prepay_card", prepay_card);
						infoMap.put("add_card", add_card);
						infoMap.put("refund_card", refund_card);
						infoMap.put("pursue_card", pursue_card);
						infoMap.put("pfee_card", pfee_card);
						infoMap.put("id", id);
						infoList.add(infoMap);
					}
				}
			}
			return infoList;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	/**
	 * ͳһ�ӿ�,ͳ��ͣ����
	 * @param type	1���ֽ�2���շ�Ա�˻������շѣ�3�������˻������շѣ�4����Ӫ�����˻������շѣ�5���̻��˻������շѣ�6����δ׷�ɶ�����7���鳵�����������
	 * @param startTime	��ʼʱ��
	 * @param endTime	����ʱ��
	 * @param groupby	�����ѯ���ֶ�
	 * @param sqlInfo	��������������
	 * @param otherMap	�ǻ�������������д������
	 * @return
	 */
	public List<Map<String, Object>> anlysisMoney(int type, Long startTime, Long endTime, 
			String[] groupby, SqlInfo sqlInfo, Map<String, Object> otherMap){
		List<Map<String, Object>> result= null;
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = null;
			String condSql = "";
			params.add(startTime);
			params.add(endTime);
			String ogroupSql = groupSql(groupby);//��ѯ�����ֶ�
			String groupSql = "";
			if(!"".equals(ogroupSql)){
				groupSql = " group by " + ogroupSql.substring(1);
			}
			if(sqlInfo!=null){//������������
				condSql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			switch (type) {
			case 1://���ֽ�
				sql = "select sum(a.amount) summoney "+ogroupSql+" from parkuser_cash_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 2://���շ�Ա�˻������շ�
				sql = "select sum(a.amount) summoney "+ogroupSql+" from parkuser_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? " + condSql +
						" and a.remark like ? "+ groupSql;
				params.add("ͣ����%");
				break;
			case 3://�鳵���˻������շ�
				sql = "select sum(a.amount) summoney "+ogroupSql+" from park_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 4://����Ӫ�����˻������շ�
				sql = "select sum(a.amount) summoney "+ogroupSql+" from group_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 5://���̻��˻������շ�
				sql = "select sum(a.amount) summoney "+ogroupSql+" from city_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 6://��δ׷�ɽ��
				sql = "select sum(total) summoney "+ogroupSql+" from no_payment_tb where end_time " +
						" between ? and ? "+condSql+" and state=? "+groupSql;
				params.add(0);
				break;
			case 7://�鳵�����������
				sql = "select sum(total) summoney "+ogroupSql+" from berth_order_tb where out_time" +
						" between ? and ? "+condSql + " " + groupSql;
				break;
			case 8://���ӵ���δ׷�ɵĶ����ֽ�Ԥ���Ľ��
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,parkuser_cash_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql + 
						" and o.state=? and a.target=? " + groupSql;
				params.add(0);//δ׷��
				params.add(1);//Ԥ��ͣ����
				break;
			case 9://���ӵ���δ׷�ɵĶ�������Ԥ���Ľ��
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,parkuser_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql + 
						" and o.state=? and a.target=? " + groupSql;
				params.add(0);//δ׷��
				params.add(6);//Ԥ��ͣ����
				break;
			case 10://���ӵ���δ׷�ɵĶ�������Ԥ���Ľ��
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,park_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql +
						" and o.state=? and a.source=? " + groupSql;
				params.add(0);//δ׷��
				params.add(8);//Ԥ��ͣ����
				break;
			case 11://���ӵ���δ׷�ɵĶ�������Ԥ���Ľ��
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,group_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql +
						" and o.state=? and a.source=? " + groupSql;
				params.add(0);//δ׷��
				params.add(3);//Ԥ��ͣ����
				break;
			case 12://���ӵ���δ׷�ɵĶ�������Ԥ���Ľ��
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,city_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql +
						" and o.state=? and a.source=? " + groupSql;
				params.add(0);//δ׷��
				params.add(3);//Ԥ��ͣ����
				break;
			case 13://��ѯˢ�����
				sql = "select sum(a.amount) summoney "+ogroupSql+" from card_account_tb a,order_tb o " +
						" where a.orderid=o.id and a.create_time between ? and ? "+condSql + groupSql;
				break;
			case 14://���ӵ���δ׷�ɵ�ˢ��Ԥ���Ľ��
				sql = "select sum(a.amount) summoney "+ogroupSql+" from no_payment_tb o,card_account_tb a" +
						" where o.order_id=a.orderid and o.end_time between ? and ? " + condSql + 
						" and o.state=? and a.consume_type=? " + groupSql;
				params.add(0);//δ׷��
				params.add(1);//Ԥ��ͣ����
				break;
			default:
				break;
			}
			if(sql != null){
				result = pgOnlyReadService.getAllMap(sql, params);
			}
		} catch (Exception e) {
			logger.error("anlysisMoney", e);
		}
		return result;
	}
	
	/**
	 * ƴ�ӷ����ֶ�sql
	 * @param groupMap
	 * @return
	 */
	private String groupSql(String[] groupby){
		String groupSql = "";//�����ֶ�
		try {
			if(groupby != null && groupby.length > 0){
				for(int i = 0; i < groupby.length; i++){
					groupSql += "," + groupby[i];
				}
			}
		} catch (Exception e) {
			logger.error("groupSql", e);
		}
		return groupSql;
	}
	
	public Map<String, Object> getBerthCount(Long groupid, Long cityid){
		String sql = "select sum(share_count) asum,sum(used_count) usum from park_anlysis_tb where ";
		List<Object> params = new ArrayList<Object>();
		List<Object> parks = null;
		if(cityid > 0){
			parks = getparks(cityid);
		}else if(groupid > 0){
			parks = getParks(groupid);
		}
		if(parks != null && !parks.isEmpty()){
			String preParams  ="";
			for(Object parkid : parks){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			params.addAll(parks);
			sql += " comid in ("+preParams+") and create_time=(select max(create_time) from park_anlysis_tb)";
			Map<String, Object> map = pgOnlyReadService.getMap(sql, params);
			return map;
		}
		return null;
	}
	
	/**
	 * ��ȡ�յ�������״̬
	 * @param list
	 */
	public void getState(List<Map<String, Object>> list){
		Long ntime = System.currentTimeMillis()/1000;
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				int state = 0;
				if(map.get("heartbeat_time") == null){
					state = 1;
				}else{
					Long heartbeat_time = (Long)map.get("heartbeat_time");
					if(ntime - heartbeat_time > 30 * 60){
						state = 1;
					}
				}
				map.put("induce_state", state);
			}
		}
	}
	
	/**
	 * �����˺Ż�ȡ���ƺ�
	 * @param uin
	 * @return
	 */
	public String getcar(Long uin){
		String cars = "��";
		if(uin!=-1){
			List<Map<String, Object>> carList = daService.getAll("select car_number " +
					" from car_info_tb where uin =? ", new Object[]{uin});
			if(carList!=null&&!carList.isEmpty()){
				cars = "";
				for(Map<String, Object> map :carList){
					cars += map.get("car_number")+",";
				}
				if(cars.endsWith(","))
					cars =cars.substring(0,cars.length()-1); 
			}
		}
		return cars;
	}
	
	/**
	 * ���ó�λ�Ƿ�ռ��
	 * @param comid
	 * @param plot
	 * @param btime
	 * @param etime
	 * @param cid
	 * @return
	 */
	public String checkplot(Long comid, String plot, Long btime, Long etime, Long cid){
		logger.error("check plots>>>comid:"+comid+",plot:"+plot+",btime:"+btime+",etime:"+etime);
		String r = null;
		if(plot != null && !plot.equals("")){
			Long count = daService.getLong("select count(id) from com_park_tb where cid=? and comid=? ", 
					new Object[]{plot, comid});
			logger.error("check plots>>>count:"+count);
			if(count == 0){
				r = "��λ"+plot+"������";
			}
			count = pgOnlyReadService.getLong("select count(cp.id) from carower_product cp,product_package_tb p where cp.pid=p.id and ((cp.b_time<=? and cp.e_time>?) or (cp.b_time<? and cp.b_time>=?)) and cp.p_lot=? and p.comid=? and cp.id!=? ", 
					new Object[]{btime, btime, etime, btime, plot, comid, cid});
			logger.error("check plots>>>count:"+count);
			if(count > 0){
				r = "��ǰʱ����ڣ���λ"+plot+"�ѱ�ռ�ã��Ƽ���λ��ţ�";
			}
			if(r != null){
				List<Map<String, Object>> list = pgOnlyReadService.getAll("select cid from com_park_tb where comid=? ", 
						new Object[]{comid});
				List<Map<String, Object>> list2 = pgOnlyReadService.getAll("select cp.p_lot from carower_product cp,product_package_tb p where cp.pid=p.id and ((cp.b_time<=? and cp.e_time>?) or (cp.b_time<? and cp.b_time>=?)) and p.comid=? and cp.id!=? and cp.p_lot is not null ", 
						new Object[]{btime, btime, etime, btime, comid, cid});
				List<String> allplots = new ArrayList<String>();
				List<String> usedplots = new ArrayList<String>();
				for(Map<String, Object> map : list){
					if(map.get("cid") != null){
						allplots.add((String)map.get("cid"));
					}
				}
				for(Map<String, Object> map : list2){
					if(map.get("p_lot") != null){
						usedplots.add((String)map.get("p_lot"));
					}
				}
				for(int i = 0;i<allplots.size(); i++){
					String p_lot = allplots.get(i);
					if(i > 5){
						break;
					}
					if(!usedplots.contains(p_lot)){
						if(i == 0){
							r += p_lot;
						}else{
							r += ","+p_lot;
						}
					}
				}
			}
		}
		logger.error("check plots>>>r:"+r);
		return r;
	}
	/**
	 * ��ȡ��Ӫ����Ͻ�µĳ���������Ӫ����Ͻ�µ�������µĳ���
	 * @param groupid
	 * @return
	 */
	public List<Object> getParks(Long groupid){
		List<Object> parks = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id from com_info_tb where state<>? and groupid=? " ;
			params.add(1);
			params.add(groupid);
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					parks.add(map.get("id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parks;
	}
	/**
	 * ��ȡ���е��µ���Ӫ���ű��
	 * @param cityid
	 * @return
	 */
	public List<Object> getGroups(Long cityid){//��ѯ������Ͻ����Ӫ����
		List<Object> groups = new ArrayList<Object>();
		List<Map<String, Object>> list = pgOnlyReadService.getAll("select id from org_group_tb" +
				" where cityid=? and state=? ", 
				new Object[]{cityid, 0});
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				groups.add(map.get("id"));
			}
		}
		return groups;
	}
	
	/**
	 * ��ȡ��Ӫ���ŵ��µ�����
	 * @param groups
	 * @return
	 */
	public List<Object> getAreas(List<Object> groups){//��ѯ����ֱ������ͳ�����Ͻ�������µ�����
		List<Object> areas = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select id from org_area_tb where state=? ";
		params.add(0);
		if(groups != null && !groups.isEmpty()){
			String preParams  ="";
			for(Object grouid : groups){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " and groupid in ("+preParams+")";
			params.addAll(groups);
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					areas.add(map.get("id"));
				}
			}
		}
		return areas;
	}
	
	/**
	 * ��ȡ���е��µĳ���
	 * @param cityid
	 * @return
	 */
	public List<Object> getparks(Long cityid){
		List<Object> parks = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id from com_info_tb where state<>? " ;
			params.add(1);
			List<Object> groups = getGroups(cityid);//��ѯ�ó�����Ͻ����Ӫ����
			if(groups != null && !groups.isEmpty()){
				String preParams  ="";
				for(Object grouid : groups){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and groupid in ("+preParams+") ";
				params.addAll(groups);
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
				if(list != null && !list.isEmpty()){
					for(Map<String, Object> map : list){
						parks.add(map.get("id"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parks;
	}
	
	/**
	 * ��ȡ��Ӫ���ŵ��շ�Ա
	 */
	public List<Object> getCollctors(Long groupid){
		List<Object> collectors = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			params.add(1);
			params.add(2);
			params.add(groupid);
			String sql = "select id from user_info_tb where state<>? and (auth_flag=? or auth_flag=?)" +
					" and groupid=? " ;
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					collectors.add(map.get("id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collectors;
	}
	
	public List<Object> getcollctors(Long cityid){
		List<Object> collectors = new ArrayList<Object>();
		try {
			List<Object> params = new ArrayList<Object>();
			String sql = "select id from user_info_tb where state<>? and (auth_flag=? or auth_flag=?)" +
					" and (cityid=? " ;
			params.add(1);
			params.add(1);
			params.add(2);
			params.add(cityid);
			List<Object> groups = getGroups(cityid);//��ѯ�ó�����Ͻ����Ӫ����
			if(groups != null && !groups.isEmpty()){
				String preParams  ="";
				for(Object grouid : groups){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " or groupid in ("+preParams+") ";
				params.addAll(groups);
			}
			sql += ")";
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					collectors.add(map.get("id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collectors;
	}
	
	/**
	 * ��ȡ�����Ĳ�λ�α��
	 * @param parks
	 * @return
	 */
	public List<Object> getBerthSeg(List<Object> parks){
		List<Object> berthseg = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		String sql = "select id from com_berthsecs_tb where is_active=? " ;
		params.add(0);
		if(parks != null && !parks.isEmpty()){
			String preParams  ="";
			for(Object park : parks){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			sql += " and comid in ("+preParams+") ";
			params.addAll(parks);
			
			List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql,params);
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					berthseg.add(map.get("id"));
				}
			}
		}
		return berthseg;
	}
	
	//���³�λ��Ϣ�������ѽ��㶩����ռ�ó�λ
	public void updateParkInfo(Long comId) {
		int r =daService.update("update com_park_tb set state =?,order_id=? where order_id in " +
				"(select id from order_tb where state in(?,?) and id in(select order_id from com_park_tb where comid=?)) ",
				new Object[]{0,null,1,2,comId});
		logger.error(comId+"��������"+r+"����λ��Ϣ");
	}
	
	
	//��ѯ���
	public boolean checkBonus(String mobile,Long uin){
		List bList = pgOnlyReadService.getAll("select * from bonus_record_tb where mobile=? and state=? ",new Object[]{mobile,0});
		String tsql = "insert into ticket_tb (create_time,limit_day,money,state,uin,type) values(?,?,?,?,?,?) ";
		List<Object[]> values = new ArrayList<Object[]>();
		if(bList!=null&&bList.size()>0){
			Long bid = null;
			for(int i=0;i<bList.size();i++){
				Map map = (Map)bList.get(i);
				Long _bid = (Long)map.get("bid");
				if(_bid!=null&&_bid>0)
					bid = _bid;
				Integer money = (Integer)map.get("amount");
				
				Integer type = (Integer)map.get("type");
				Long ctime = TimeTools.getToDayBeginTime();//(Long)map.get("ctime");
				Long etime = ctime+6*24*60*60-1;
				
				if(type==1){//΢�Ŵ���ȯ
					values.add(new Object[]{ctime,etime,money,0,uin,2});
				}else {//��ͨͣ��ȯ
					if(money==30||money==100){//3��10Ԫȯ
						if(money==30){
							values.add(new Object[]{ctime,etime,4,0,uin,0});
							values.add(new Object[]{ctime,etime,4,0,uin,0});
							values.add(new Object[]{ctime,etime,1,0,uin,0});
							values.add(new Object[]{ctime,etime,1,0,uin,0});
							values.add(new Object[]{ctime,etime,3,0,uin,0});
							values.add(new Object[]{ctime,etime,3,0,uin,0});
							values.add(new Object[]{ctime,etime,2,0,uin,0});
							values.add(new Object[]{ctime,etime,2,0,uin,0});
							values.add(new Object[]{ctime,etime,4,0,uin,0});
							values.add(new Object[]{ctime,etime,1,0,uin,0});
							values.add(new Object[]{ctime,etime,3,0,uin,0});
							values.add(new Object[]{ctime,etime,2,0,uin,0});
						}else {
							int end = 10;
							for(int j=0;j<end;j++){
								values.add(new Object[]{ctime,etime,10,0,uin,0});
							}
						}
					}else if(money==10){//1��10Ԫȯ
						values.add(new Object[]{ctime,etime,4,0,uin,0});
						values.add(new Object[]{ctime,etime,1,0,uin,0});
						values.add(new Object[]{ctime,etime,3,0,uin,0});
						values.add(new Object[]{ctime,etime,2,0,uin,0});
					}else {
						Object[] v1 = new Object[]{ctime,etime,money,0,uin,0};
						values.add(v1);
					}
				}
			}
			if(values.size()>0){
				int ret= daService.bathInsert(tsql, values, new int[]{4,4,4,4,4,4});
				logger.error("�˻�:"+uin+",�ֻ���"+mobile+",�û���¼ ��д����ͣ��ȯ"+ret+"��");
				logger.error(">>>>�û�������ȯ�����º����¼��"+daService.update("update bonus_record_tb set state=? where mobile=?", new Object[]{1,mobile}));
				if(ret>0){
					//���³���ע��ý����Դ 0������ע�ᣬ1-997�Ƕ��ƺ����1����ͷ���������������2�������,3���պ��.4.����ͷ������أ�����998ֱ�����,999���շ�Ա�Ƽ���1000�����ǳ��������������
					if(bid!=null&&bid>0){
						Integer media = 0;
						if(bid>999){//1000���ϵı���ǳ��������������������Ϊ���ƺ������д���û���
							media=1000;
						}else {
							media = bid.intValue();
						}
						if(media>0){//����ý����Դ
							daService.update("update user_info_tb set media=? where id=? ", new Object[]{media,uin});
						}
					}
					return true;
				}
			}
		}else {
			logger.error("�˻�:"+uin+",�ֻ���"+mobile+",û�к��....");
		}
		return false;
	}
	/**
	 * ȡ����ͣ��ȯ��δ��֤�������ʹ��3Ԫȯ��
	 * 	 * 9Ԫ��ͣ���ѣ� Ҳ����ʹ��18Ԫ��ͣ��ȯ����ֻ�ֿܵ�8Ԫ��  
		���8����Ƕ�̬�ķ�������ȡ����Ϊ�п���ѹ�������������Ż�ȯֻ�ֿۣܵ�ͣ����-2����8�ͱ�Ϊ7�ˡ�
	 * @param uin
	 * @param fee
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getTickets(Long uin,Double fee,Long comId,Long uid){
		//������п��õ�ȯ
		//Long ntime = System.currentTimeMillis()/1000;
		Integer limit = CustomDefind.getUseMoney(fee,0);
		Double splimit = StringUtils.formatDouble(CustomDefind.getValue("TICKET_LIMIT"));
		boolean blackuser = isBlackUser(uin);
		boolean blackparkuser =false;
		if(comId!=null)
			blackparkuser=publicMethods.isBlackParkUser(comId, false);
		boolean isauth = publicMethods.isAuthUser(uin);
		if(!isauth){
			if(blackuser||blackparkuser){
				if(blackuser){
					logger.error("�����ں�������uin:"+uin+",fee:"+fee+",comid:"+comId);
				}
				if(blackparkuser){
					logger.error("�����ں�������uin:"+uin+",fee:"+fee+",comid:"+comId);
				}
				return null;
			}
		}else{
			logger.error("����uin:"+uin+"����֤��������ȯ���ж��Ƿ��Ǻ������������Ƿ��������");
		}
		List<Map<String, Object>> list = null;
		double ticketquota=-1;
		if(uid!=-1){
			Map usrMap =daService.getMap("select ticketquota from user_info_Tb where id =? and ticketquota<>?", new Object[]{uid,-1});
			if(usrMap!=null){
				ticketquota = Double.parseDouble(usrMap.get("ticketquota")+"");
			}
		}
		logger.error("���շ�Ա:"+uid+"����ȯ����ǣ�"+ticketquota+"��(-1����û����)");
		if(!isauth){//δ��֤�������ʹ��2Ԫȯ��
			double noAuth = 1;//δ��֤�����������noAuth(2)Ԫȯ,�Ժ�Ķ����ֵ��ok
			if(ticketquota>=0&&ticketquota<=noAuth){
//				ticketquota = ticketquota+1;
			}else{
				ticketquota=noAuth;
			}
			list=	pgOnlyReadService.getAll("select * from ticket_tb where uin = ? " +
					"and state=? and limit_day>=? and type<? and money<?  order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2,ticketquota+1});

		}else {
			list  = pgOnlyReadService.getAll("select * from ticket_tb where uin = ? " +
					"and state=? and limit_day>=? and type<=?  order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2});
		}
		logger.error("uin:"+uin+",fee:"+fee+",comid:"+comId+",today:"+TimeTools.getToDayBeginTime());
		if(list!=null&&!list.isEmpty()){
			List<String> _over3day_moneys = new ArrayList<String>();
			int i=0;
			for(Map<String, Object> map : list){
				Integer money = (Integer)map.get("money");
				//Long limit_day = (Long)map.get("limit_day");
				Long tcomid = (Long)map.get("comid");
				Integer type = (Integer)map.get("type");
//				logger.error("ticket>>>uin:"+uin+",comId:"+comId+",tcomid:"+tcomid+",type:"+type+",ticketid:"+map.get("id"));
				if(comId!=null&&comId!=-1&&tcomid!=null&&type == 1){
					if(comId.intValue()!=tcomid.intValue()){
						logger.error(">>>>get ticket:�������������ͣ��ȯ��������....comId:"+comId+",tcomid:"+tcomid+",uin:"+uin);
						i++;
						continue;
					}
				}
				Integer res = (Integer)map.get("resources");
				if(limit==0&&res==0&&type==0){//֧�����С��3Ԫ��������ͨȯ
					i++;					
					continue;
				}
				if(type==1||res==1){
					limit=Double.valueOf((fee-splimit)).intValue();
				}else {
					limit= CustomDefind.getUseMoney(fee,0);
				}
				map.put("isbuy", res);
				if(money==limit){//ȯֵ+1Ԫ ���� ֧�����ʱֱ�ӷ���
					return map;
				}
				//�ж� �Ƿ� �� ���Ǹó�����ר��ȯ
				
				map.remove("comid");
//				map.remove("limit_day");
				_over3day_moneys.add(i+"_"+Math.abs(limit-money));
				i++;
			}
			if(_over3day_moneys.size()>0){//ͣ��ȯ��ͣ���ѵľ���ֵ���� ��ȡ����ֵ��С��
				int sk = 0;//����index
				double sv=0;//������Сֵ
				int index = 0;
				for(String s : _over3day_moneys){
					int k = Integer.valueOf(s.split("_")[0]);
					double v = Double.valueOf(s.split("_")[1]);
					if(index==0){
						sk=k;
						sv = v;
					}else {
						if(sv>v){
							sk=k;
							sv = v;
						}
					}
					index++;
				}
				logger.error("uin:"+uin+",comid:"+comId+",sk:"+sk);
				return list.get(sk);
			}
		}else{
			logger.error("δѡ��ȯuin:"+uin+",comid:"+comId+",fee:"+fee);
		}
		return null;
	}
	
	/**
	 * ����ʺ��Ƿ���ע��
	 * @param strid
	 * @return
	 */
	public boolean checkStrid(String strid){
		String sql = "select count(*) from user_info_tb where strid =?";
		Long result = daService.getLong(sql, new Object[]{strid});
		if(result>0){
			return false;
		}
		return true;
	}
	
	public boolean checkStrid(String strid, Long uin){
		String sql = "select count(*) from user_info_tb where strid =? and id<>? ";
		Long result = daService.getLong(sql, new Object[]{strid, uin});
		if(result>0){
			return false;
		}
		return true;
	}
	
	/**
	 * ������²�Ʒ����
	 * @param prodId ���²�Ʒ���
	 * @param months ��������
	 * @return
	 */
	public Double getProdSum(Long prodId, Integer months){
		Double total = 0d;
		if(prodId != null && prodId > 0 && months != null && months > 0){
			Double price = 0d;
			Map<String, Object> pMap = daService.getMap("select limitday,price from product_package_tb where id=? ", 
					new Object[]{prodId});
			if(pMap!=null&&pMap.get("limitday")!=null){
				price = Double.valueOf(pMap.get("price")+"");
			}
			total = months*price;
		}
		return total;
	}
	
	/**�Ƿ��ں�����*/
	public boolean isBlackUser(Long uin){
		List<Long> blackUserList = memcacheUtils.doListLongCache("zld_black_users", null, null);
		boolean isBlack = true;
		if(blackUserList==null||!blackUserList.contains(uin))//���ں������п��Դ����Ƽ�����
			isBlack=false;
		return isBlack;
	}
	
	/**
	 * ����openid��ȡ�û���Ϣ
	 * @param openid
	 * @return
	 */
	public Map<String, Object> getUserByOpenid(String openid){
		Map<String, Object> userMap = daService.getMap("select * from user_info_tb where wxp_openid=? limit ? ",
				new Object[] { openid, 1 });
		return userMap;
	}
	
	/**
	 * ����openid��ȡ�û�����Ϣ
	 * @param openid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUserinfoByOpenid(String openid){
		Map<String, Object> map = new HashMap<String, Object>();
		Integer bindflag = 0;
		Long uin = -1L;
		String mobile = "";
		Double balance = 0d;
		Map<String, Object> userMap = daService.getMap("select * from user_info_tb where wxp_openid=? limit ? ",
				new Object[] { openid, 1 });
		if(userMap != null){
			bindflag = 1;
			uin = (Long)userMap.get("id");
			mobile = (String)userMap.get("mobile");
			balance = Double.valueOf(userMap.get("balance") + "");
		}else{
			userMap = daService.getMap("select * from wxp_user_tb where openid=? limit ? ", new Object[]{openid, 1});
			if(userMap == null){
				uin = daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid",null);
				int r = daService.update("insert into wxp_user_tb(openid,create_time,uin) values(?,?,?) ",
								new Object[] { openid, System.currentTimeMillis() / 1000, uin});
				logger.error("û����ʱ�˻�������һ��uin:"+uin+",openid:"+openid+",r:"+r);
			}else{
				uin = (Long)userMap.get("uin");
				balance = Double.valueOf(userMap.get("balance") + "");
			}
		}
		map.put("bindflag", bindflag);
		map.put("uin", uin);
		map.put("mobile", mobile);
		map.put("balance", balance);
		return map;
	}
	
	/**
	 * ɨ����ȯ����ȡ����ǰ���ͣ���ѽ��
	 * @param orderMap
	 * @param shopTicketMap
	 * @param delaytime Ԥ֧����ʱʱ��
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPrice(Long orderId, Long end_time){
		Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ", 
				new Object[]{orderId});
		
		Map<String, Object> map = new HashMap<String, Object>();
		Long comid = (Long)orderMap.get("comid");
		Double beforetotal = 0d;
		Double aftertotal = 0d;
		Integer car_type = (Integer)orderMap.get("car_type");//0��ͨ�ã�1��С����2����
		Integer pid = (Integer)orderMap.get("pid");
		Long create_time = (Long)orderMap.get("create_time");
		Integer distime = 0;//�ֿ۵�ʱ��
		
		beforetotal = getPrice(car_type, pid, comid, create_time, end_time);
		
		Map<String, Object> shopTicketMap = daService.getMap("select * from ticket_tb where orderid=? and (type=? or type=?) ", 
				new Object[]{orderId, 3, 4});
		if(shopTicketMap != null){
			Integer type = (Integer)shopTicketMap.get("type");
			if(type == 3){
				Integer time = (Integer)shopTicketMap.get("money");
				if(end_time > create_time + time *60 *60){
					aftertotal = getPrice(car_type, pid, comid, create_time, end_time - time * 60 *60);
					distime =time *60 *60;
				}else if(end_time > create_time){
					distime = (end_time.intValue() - create_time.intValue());
				}
			}else if(type == 4){
				if(end_time > create_time){
					distime = (end_time.intValue() - create_time.intValue());
				}
			}
		}else{
			aftertotal = beforetotal;
		}
		
		Double distotal = beforetotal - aftertotal >0 ? (beforetotal - aftertotal) : 0d;
		
		if(shopTicketMap != null && beforetotal > aftertotal){
			int r = daService.update("update ticket_tb set umoney=?,bmoney=? where id=? ", 
					new Object[]{StringUtils.formatDouble(distotal), Double.valueOf(distime)/(60*60), shopTicketMap.get("id")});
		}
		map.put("beforetotal", beforetotal);
		map.put("aftertotal", aftertotal);
		return map;
	}
	
	/**
	 * ���ݶ�����Ϣ��ȡ���ѽ��
	 * @param car_type
	 * @param pid
	 * @param comid
	 * @param create_time
	 * @param end_time
	 * @return
	 */
	public Double getPrice(Integer car_type, Integer pid, Long comid, Long create_time, Long end_time){
		Double total = 0d;
		if(pid>-1){
			total = Double.valueOf(publicMethods.getCustomPrice(create_time, end_time, pid));
		}else {
			total = Double.valueOf(publicMethods.getPrice(create_time, end_time, comid, car_type));
		}
		return total;
	}
	
	/**
	 * ��ȡ��������Ϣ
	 * @param orderId
	 * @param shopTicketId ����ȯID
	 * @param uin �û�ID
	 * @param delaytime Ԥ֧�����ӳ�ʱ��
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderInfo(Long orderId, Long shopTicketId, Long end_time){
		Double pretotal = 0d;//�Ѿ�Ԥ֧���Ľ��
		Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? ", 
				new Object[]{orderId});
		if(orderMap == null){
			return null;
		}
		if(orderMap.get("total") != null){
			pretotal = Double.valueOf(orderMap.get("total") + "");//Ԥ֧���Ľ��
		}
		Long create_time = (Long)orderMap.get("create_time");
		Map<String, Object> map = getComOrderInfo(orderId, shopTicketId, create_time, end_time);
		
		map.put("createtime", create_time);
		map.put("starttime", TimeTools.getTime_yyyyMMdd_HHmm(create_time * 1000));
		map.put("parktime", StringUtils.getTimeString(create_time, System.currentTimeMillis()/1000));
		map.put("pretotal", pretotal);
		map.put("shopticketid", shopTicketId);
		map.put("uid", orderMap.get("uid"));
		map.put("comid", orderMap.get("comid"));
		map.put("carnumber", orderMap.get("car_number"));
		return map;
	}
	
	/**
	 * ��ȡ�ѽ��㶩������Ϣ
	 * @param orderid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderInfoPayed(Long orderid, Long shopTicketId){
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> orderMap = daService.getMap("select * from order_tb where id=? and state=? ", 
				new Object[]{orderid, 1});
		if(orderMap == null){
			return null;
		}
		Long create_time = (Long)orderMap.get("create_time");
		Long end_time = (Long)orderMap.get("end_time");
		Double total = Double.valueOf(orderMap.get("total") + "");
		map = getComOrderInfo(orderid, shopTicketId, create_time, end_time);
		
		map.put("createtime", create_time);
		map.put("starttime", TimeTools.getTime_yyyyMMdd_HHmm(create_time * 1000));
		map.put("parktime", StringUtils.getTimeString(create_time, end_time));
		map.put("total", total);
		map.put("uid", orderMap.get("uid"));
		map.put("comid", orderMap.get("comid"));
		map.put("carnumber", orderMap.get("car_number"));
		map.put("shopticketid", shopTicketId);
		map.put("paytype", orderMap.get("pay_type"));
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getComOrderInfo(Long orderid, Long shopTicketId, Long create_time, Long end_time){
		Map<String, Object> map = new HashMap<String, Object>();
		Double beforetotal = 0d;//����֮ǰ��ͣ���ѽ��
		Double aftertotal = 0d;//����֮���ͣ���ѽ��
		
		Integer tickettype = 3;//����ȯ���ͣ�Ĭ�ϼ�ʱȯ
		Integer tickettime = 0;//��ʱȯ��ʱ��
		Integer ticketstate = 0;//����ȯ��״̬��0�������� 1:����
		Map<String, Object> shopTicketMap = daService.getMap("select * from ticket_tb where orderid=? and (type=? or type=?) ", 
				new Object[]{orderid, 3, 4});
		if(shopTicketMap == null){
			if(shopTicketId != null && shopTicketId > 0){
				shopTicketMap = daService.getMap("select * from ticket_tb where id=? and (orderid=? or orderid=?) and state=? and (type=? or type=?) and limit_day>? ", 
						new Object[]{shopTicketId, -1, orderid, 0, 3, 4, end_time});
			}
		}else{
			shopTicketId = (Long)shopTicketMap.get("id");
		}
		if(shopTicketMap != null){
			int r = daService.update("update ticket_tb set orderid=? where id=? ", new Object[]{orderid, shopTicketId});
			tickettype = (Integer)shopTicketMap.get("type");
			tickettime = (Integer)shopTicketMap.get("money");
			ticketstate = 1;//�ü���ȯ����
		}
		Map<String, Object> map2 = getPrice(orderid, end_time);
		beforetotal = Double.valueOf(map2.get("beforetotal") + "");
		aftertotal = Double.valueOf(map2.get("aftertotal") + "");
		map.put("beforetotal", beforetotal);
		map.put("aftertotal", aftertotal);
		map.put("ticketstate", ticketstate);
		map.put("tickettype", tickettype);
		map.put("tickettime", tickettime);
		return map;
	}
	
	/**
	 * �����û�ID��ȡ����ʱ�˻�������ʽ�˻�
	 * @param uin
	 * @return
	 */
	public Integer getBindflag(Long uin){
		Long count = daService.getLong("select count(1) from user_info_tb where id=? ", new Object[]{uin});
		return count.intValue();
	}
	
	public Integer addCarnumber(Long uin, String carnumber){
		Long cutTime = System.currentTimeMillis()/1000;
		Integer bindflag = getBindflag(uin);
		if(bindflag == 1){
			Long count = daService.getLong("select count(*) from car_info_tb where uin!=? and car_number=? and state=? ",
					new Object[] { uin, carnumber, 1 });
			if(count > 0){//�ó��ƺ��ѱ�����ע��
				return -1;
			}
			count = daService.getLong("select count(*) from car_info_tb where uin=? and car_number=? ",
					new Object[] { uin, carnumber});
			if(count > 0){//�ó����Ѿ�ע����ó��ƺ�
				return -2;
			}else{
				count = daService.getLong("select count(*) from car_info_tb where uin=? ",
						new Object[] { uin });
				if(count >= 3){//�ó���ע��ĳ��ƺŵĸ���
					return -3;
				}
				int r=daService.update("insert into car_info_Tb (uin,car_number,create_time) values(?,?,?)", 
						new Object[]{uin, carnumber, cutTime});
				if(r > 0){
					return 1;
				}
			}
		}else if(bindflag == 0){
			int r = daService.update("update wxp_user_tb set car_number=? where uin=? ", 
					new Object[]{carnumber, uin});
			if(r > 0){
				return 1;
			}
		}
		return -4;
	}
	
	/**
	 * ��������վ����Ϊ��Ҫ����������ʱ�򴴽�Ĭ�Ϲ���վ�����Գ����һ��������
	 * @param request
	 * @param comid
	 * @param cname
	 * @return
	 */
	public Long createWorksite(HttpServletRequest request, Map<String, Object> map){
		try {
			Long operater = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_worksite_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_worksite_tb(id,comid,worksite_name,description,net_type) values(?,?,?,?,?)";
			int r = daService.update(sql, new Object[]{nextid,comid,map.get("worksite_name"),map.get("description"),map.get("net_type")});
			logger.error("parkadmin or admin:"+operater+" add comid:"+map.get("comid")+" worksite");
			if(r == 1){
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_worksite_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" worksite ,add sync ret:"+re);
				}
				mongoDbUtils.saveLogs( request,0, 2, "�����˹���վ��"+map.get("worksite_name"));
				return nextid;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0L;
	}
	
	/**
	 * ����ͨ������Ϊ��Ҫ����������ʱ�򴴽�Ĭ��ͨ�������Գ����һ��������
	 * @param request
	 * @param map
	 * @return
	 */
	public Long createPass(HttpServletRequest request, Map<String, Object> map){
		try {
			Long operater = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_pass_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_pass_tb(id,worksite_id,comid,passname,passtype,description,month_set,month2_set) values(?,?,?,?,?,?,?,?)";
			int r = daService.update(sql, new Object[]{nextid,map.get("worksite_id"),comid,map.get("passname"),map.get("passtype"),map.get("description"),map.get("month_set"),map.get("month2_set")});
			logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" pass ");
			if(r == 1){
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_pass_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+operater+" add comid:"+comid+" pass ,add sync ret:"+re);
				}
				mongoDbUtils.saveLogs( request,0, 2, "������ͨ��:"+map.get("passname"));
				return nextid;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0L;
	}
	
	/**
	 * ��������ͷ����Ϊ��Ҫ����������ʱ�򴴽�Ĭ������ͷ�����Գ����һ��������
	 * @param request
	 * @param map
	 * @return
	 */
	public Integer createCamera(HttpServletRequest request, Map<String, Object> map){
		try {
			Long nickname = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			//����
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_camera_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_camera_tb(id,passid,camera_name,ip,port,cusername,manufacturer,comid) values(?,?,?,?,?,?,?,?)";
			int re = daService.update(sql, new Object[]{nextid,map.get("passid"),map.get("camera_name"),map.get("ip"),map.get("port"),map.get("cusername"),map.get("manufacturer"),comid});
			if(re == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" camera ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" camera ");
				}
				mongoDbUtils.saveLogs( request,0, 2, "����������ͷ:"+map.get("camera_name"));
				return 1;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0;
	}
	
	public Integer createLED(HttpServletRequest request, Map<String, Object> map){
		try {
			Long nickname = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)map.get("comid");
			//����
			Long nextid = daService.getLong(
					"SELECT nextval('seq_com_led_tb'::REGCLASS) AS newid", null);
			String sql = "insert into com_led_tb(id,passid,ledip,ledport,leduid,movemode,movespeed,dwelltime,ledcolor,showcolor,typeface,typesize,matercont,width,height,type,rsport,comid) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			int re = daService.update(sql, new Object[]{nextid,map.get("passid"),map.get("ledip"),map.get("ledport"),map.get("leduid"),map.get("movemode"),map.get("movespeed"),map.get("dwelltime"),map.get("ledcolor"),map.get("showcolor"),map.get("typeface"),map.get("typesize"),map.get("matercont"),map.get("width"),map.get("height"),map.get("type"),map.get("rsport"),comid});
			
			if(re == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",nextid,System.currentTimeMillis()/1000,0});
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" led ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" add comid:"+comid+" led");
				}
				mongoDbUtils.saveLogs(request, 0, 2, "�����ˣ�comid:"+comid+"����LED��"+map.get("ledip")+":"+map.get("ledport"));
				return 1;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0;
	}
	
	/**
	 * �����³���ʱ������Ĭ�ϵĹ���վ����Ϣ
	 * @param request
	 * @param map
	 */
	public void createDefDevice(HttpServletRequest request, Map<String, Object> map){
		try {
			Long comid = (Long)map.get("comid");
			String cname = (String)map.get("cname");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("comid", comid);
			params.put("worksite_name", cname);
			params.put("description", cname);
			params.put("net_type", 0);
			Long worksiteid = createWorksite(request, params);
			logger.info("create default worksite successed>>>comid:"+comid+",worksiteid:"+worksiteid);
			if(worksiteid != null && worksiteid > 0){
				params.clear();
				params.put("comid", comid);
				params.put("worksite_id", worksiteid);
				params.put("passname", cname + "���");
				params.put("passtype", 0);
				params.put("month_set", -1);
				params.put("month2_set", -1);
				Long inpassId = createPass(request, params);
				logger.info("create default inpass successed>>>comid:"+comid+",worksiteid:"+worksiteid+",inpassId:"+inpassId);
				
				params.clear();
				params.put("comid", comid);
				params.put("worksite_id", worksiteid);
				params.put("passname", cname + "����");
				params.put("passtype", 1);
				params.put("month_set", -1);
				params.put("month2_set", -1);
				Long outpassId = createPass(request, params);
				logger.info("create default outpass successed>>>comid:"+comid+",worksiteid:"+worksiteid+",outpassId:"+outpassId);
				
				if(inpassId != null && inpassId > 0){
					params.clear();
					params.put("comid", comid);
					params.put("camera_name", cname + "���");
					params.put("ip", "192.168.1.201");
					params.put("port", "554");
					params.put("manufacturer", "ͣ����");
					params.put("passid", inpassId);
					Integer result = createCamera(request, params);
					logger.info("create default incamera successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);
					
					params.clear();
					params.put("comid", comid);
					params.put("ledip", "192.168.1.203");
					params.put("ledport", "8888");
					params.put("leduid", "41");
					params.put("movemode", 9);
					params.put("movespeed", 1);
					params.put("dwelltime", 1);
					params.put("ledcolor", 1);
					params.put("showcolor", 0);
					params.put("typeface", 1);
					params.put("typesize", 1);
					params.put("matercont", "ͣ����");
					params.put("passid", inpassId);
					params.put("width", 64);
					params.put("height", 32);
					params.put("rsport", 2);
					result = createLED(request, params);
					logger.info("create default inled successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);
				}
				
				if(outpassId != null && outpassId > 0){
					params.clear();
					params.put("comid", comid);
					params.put("camera_name", cname + "����");
					params.put("ip", "192.168.1.202");
					params.put("port", "554");
					params.put("manufacturer", "ͣ����");
					params.put("passid", outpassId);
					Integer result = createCamera(request, params);
					logger.info("create default incamera successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);
					
					params.clear();
					params.put("comid", comid);
					params.put("ledip", "192.168.1.204");
					params.put("ledport", "8888");
					params.put("leduid", "41");
					params.put("movemode", 9);
					params.put("movespeed", 1);
					params.put("dwelltime", 1);
					params.put("ledcolor", 1);
					params.put("showcolor", 0);
					params.put("typeface", 1);
					params.put("typesize", 1);
					params.put("matercont", "ͣ����");
					params.put("passid", outpassId);
					params.put("width", 64);
					params.put("height", 32);
					params.put("rsport", 2);
					result = createLED(request, params);
					logger.info("create default inled successed>>>comid:"+comid+",inpassId:"+inpassId+",result:"+result);
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * @param uin          �����˻�
	 * @param total        �������
	 * @return             ����ͣ��ȯ�б�
	 */
	public List<Map<String,Object>> getUseTickets(Long uin,Double total){
		Long time = System.currentTimeMillis()/1000;
		List<Map<String,Object>> ticketList=pgOnlyReadService.getAll("select id,limit_day as limitday,money,resources," +
				"comid,type from ticket_tb where uin = ?" +
				" and limit_day >= ? and state=? and type<?  order by type desc,money,limit_day ",
				new Object[]{uin,time,0,2});
		
		Integer limit = CustomDefind.getUseMoney(total, 0);//��ͨȯ�ֿ۽��
		Integer sysLimit = Integer.valueOf(CustomDefind.getValue("TICKET_LIMIT"));//ר��ȯ������ȯ����붩���Ĳ��
		if(ticketList!=null&&!ticketList.isEmpty()){
			for(Map<String,Object> map:ticketList){
				Integer money = (Integer)map.get("money");
				Integer res = (Integer)map.get("resources");
				Integer topMoney = CustomDefind.getUseMoney(money.doubleValue(), 1);
				Integer type=(Integer)map.get("type");
				if(res==1||type==1){//����ר��ȯ����ȯ
					topMoney = money+sysLimit;
					limit = total.intValue()-sysLimit;
				}else {
					limit = CustomDefind.getUseMoney(total, 0);
					
				}
				if(topMoney<total){//����޶�С��֧�����
					limit=money;
				}
				map.put("limit", limit);
			}
		}
		//logger.error(ticketList);
		return ticketList;
	}
	
	/**
	 * ��ȡĳ���շ�Ա�ļ���ȯ������Ԥ֧�����ֽ���
	 * @param uid
	 * @param comid
	 * @param btime
	 * @param etime
	 * @param ishd
	 * @return
	 */
	public String getTicketAndCenterPay(Long uid, Long btime, Long etime,Integer ishd,Long comid) {
		double ticket = 0.0;
		double Center = 0.0;
		double cash = 0.0;
		Double pmoney = 0d;
//		String sql1 = "select sum(b.amount)money from order_tb a,parkuser_cash_tb b where  a.end_time between" +
//				" ? and ? and a.state=? and a.uid=? and a.id=b.orderid and b.type=?";
		String sql1 = "select sum(c.amount)money from (select distinct(orderid,amount,b.create_time),orderid,amount from order_tb a," +
				"parkuser_cash_tb b where a.comid=? and  a.end_time between ? and ? and a.state=? and a.uid=?  " +
				"and a.id=b.orderid and b.type=?  ";
		
		Object [] v1 = new Object[]{comid,btime,etime,1,uid,0};
		if(ishd!=null&&ishd==1){
			sql1 += " and ishd=? ";
			v1 = new Object[]{comid,btime,etime,1,uid,0,0};
		}
		sql1+=" order by orderid)c";
		Map cashmap = pgOnlyReadService.getMap(sql1,v1);
		if(cashmap!=null&&cashmap.get("money")!=null){
			cash = Double.valueOf(cashmap.get("money")+"");
		}
		
		String sql2 = "select sum(b.amount)money from order_tb a,parkuser_cash_tb b where a.comid=? and  a.end_time between" +
				" ? and ? and a.state=? and a.uid=? and a.id=b.orderid and b.type=? ";
		Object [] v2 = new Object[]{comid,btime,etime,1,uid,1};
		if(ishd!=null&&ishd==1){
			sql2 += " and ishd=? ";
			v2 = new Object[]{comid,btime,etime,1,uid,1,0};
		}
		
		Map centermap = pgOnlyReadService.getMap(sql2,v2);
		if(centermap!=null&&centermap.get("money")!=null){
			Center = Double.valueOf(centermap.get("money")+"");
		}
		
		String sql3 = "select sum(b.umoney)money from order_tb a,ticket_tb b where a.comid=? and  a.end_time between" +
				" ? and ? and a.state=? and a.uid=? and a.id=b.orderid and (b.type=3 or b.type=4)";
		Object [] v3 =new Object[]{comid,btime,etime,1,uid};
		if(ishd!=null&&ishd==1){
			sql3 += " and ishd=? ";
			v3 = new Object[]{comid,btime,etime,1,uid,0};
		}
		
		Map tickethmap = pgOnlyReadService.getMap(sql3,v3);
		if(tickethmap!=null&&tickethmap.get("money")!=null){
			ticket = Double.valueOf(tickethmap.get("money")+"");
		}
		
		Map park = pgOnlyReadService.getMap( "select sum(a.amount) total from order_tb o,park_account_tb a where o.id=a.orderid and o.end_time between ? and ? " +
				" and a.type= ? and a.source=? and a.uid=? ",new Object[]{btime,etime,0,0,uid});
		if(park!=null&&park.get("total")!=null)
			pmoney += Double.valueOf(park.get("total")+"");
	
		Map parkuser = pgOnlyReadService.getMap( "select sum(a.amount) total from order_tb o,parkuser_account_tb a where o.id=a.orderid and o.comid=? and o.end_time between ? and ? " +
				" and a.type= ? and a.uin = ? and a.target =? and a.remark like ? ",new Object[]{comid,btime,etime,0,uid,4,"ͣ����%"});//target=4����ͣ���Ѻʹ���
		if(parkuser!=null&&parkuser.get("total")!=null)
			pmoney += Double.valueOf(parkuser.get("total")+"");
		
		return ticket+"_"+Center+"_"+cash+"_"+pmoney;
	}
	//----------------����ȯѡȯ�߼�begin--------------------//
	/**
	 * ѡ�����ȯ
	 * @param uin
	 * @param uid
	 * @param total
	 * @return
	 */
	public Map<String, Object> chooseDistotalTicket(Long uin, Long uid, Double total){
		double firstorderquota = 8.0;//Ĭ�϶��
		double ditotal = 0d;//���۶��
		double disquota = StringUtils.formatDouble(firstorderquota * ditotal);//�����ۺ�ĵֿ۽��
		
		logger.error("ѡ�ۿ�ȯuin:"+uin+",uid:"+uid+",disquota:"+disquota+",firstorderquota:"+firstorderquota+",total:"+total);
		Map<String, Object> userMap2 = daService.getMap("select comid,firstorderquota from user_info_tb where id = ? ", new Object[]{uid});
		if(userMap2!=null){
			firstorderquota = Double.valueOf(userMap2.get("firstorderquota") + "");
			disquota = StringUtils.formatDouble(firstorderquota * ditotal);
		}
		logger.error("ѡ�ۿ�ȯuin:"+uin+",uid:"+uid+",firstorderquota:"+firstorderquota+",disquota:"+disquota);
		Map<String, Object> ticketMap = new HashMap<String, Object>();
		ticketMap.put("id", -100);
		Double ticket_money = Double.valueOf(StringUtils.formatDouble(total*ditotal));
		if(ticket_money > disquota){
			ticket_money =disquota;
		}
		ticketMap.put("money", ticket_money);
		logger.error("uin:"+uin+",total:"+total+",ticketMap:"+ticketMap);
		return ticketMap;
	}
	//----------------����ȯѡȯ�߼�end--------------------//
	
	//----------------����ȯѡȯ�߼�begin--------------------//
	/**
	 * 
	 * @param uin
	 * @param total
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @param uid
	 * @param isAuth
	 * @param ptype 0�˻���ֵ��1���²�Ʒ��2ͣ���ѽ��㣻3ֱ��;4���� 5����ͣ��ȯ
	 * @param parkId
	 * @param source 0:���Կͻ���ѡȯ 1�����Թ��ں�ѡȯ
	 * @return
	 */
	public List<Map<String, Object>> chooseTicket(Long uin, Double total, Integer utype, Long uid, boolean isAuth, Integer ptype, Long parkId, Long orderId, Integer source){
		List<Map<String, Object>> list = null;
		if(ptype == 4){//����ѡȯ
			list = chooseRewardTicket(uin, total, isAuth, uid, utype, ptype, parkId, orderId, source);
		}else if(ptype == -1 || ptype == 2 || ptype == 3){
			list = chooseParkingTicket(uin, total, utype, uid, isAuth, ptype, parkId, orderId, source);
		}
		return list;
	}
	
	/**
	 * ͣ������ѡȯ
	 * @param uin
	 * @param total ͣ���ѽ��
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @param uid
	 * @param isAuth
	 * @param source 0:���Կͻ���ѡȯ 1�����Թ��ں�ѡȯ
	 * @return
	 */
	public List<Map<String, Object>> chooseParkingTicket(Long uin, Double total, Integer utype, Long uid, boolean isAuth, Integer ptype, Long parkId, Long orderId, Integer source){
		List<Map<String, Object>> list = null;
		boolean isCanUserTicket = memcacheUtils.readUseTicketCache(uin);
		logger.error("choose parking pay ticket>>>uin:"+uin+",total:"+total+",utype:"+utype+",uid:"+uid+",isAuth:"+isAuth+",isCanUserTicket:"+isCanUserTicket);
		if(isCanUserTicket){
			Double moneylimit = 9999d;//ѡȯ������
			Map<String, Object> uidMap =daService.getMap("select ticketquota from user_info_Tb where id =? and ticketquota<>?", new Object[]{uid,-1});
			if(uidMap != null){
				moneylimit = Double.parseDouble(uidMap.get("ticketquota")+"");
			}
			logger.error("uin:"+uin+",uid:"+uid+",moneylimit:"+moneylimit+",isAuth:"+isAuth);
			Integer tickettype = 2;//ѡȯ����
			if(!isAuth){
				if(source == 0){
					moneylimit = 0d;
				}else if(source == 1){
					moneylimit = 0d;
				}
			}
			logger.error("uin:"+uin+",uid:"+uid+",moneylimit:"+moneylimit+",isAuth:"+isAuth+",tickettype:"+tickettype);
			list = getLimitTickets(moneylimit, tickettype, uin, utype, ptype, uid, total, parkId, orderId);
		}
		return list;
	}
	
	/**
	 * ѡ����ȯ
	 * @param uin
	 * @param total
	 * @param isAuth
	 * @param uid
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @param ptype 0�˻���ֵ��1���²�Ʒ��2ͣ���ѽ��㣻3ֱ��;4���� 5����ͣ��ȯ
	 * @param source 0:���Կͻ���ѡȯ 1�����Թ��ں�ѡȯ
	 * @return
	 */
	public List<Map<String, Object>> chooseRewardTicket(Long uin, Double total, boolean isAuth, Long uid, Integer utype, Integer ptype, Long parkId, Long orderId, Integer source){
		List<Map<String, Object>> list = null;
		Map<Long, Long> tcacheMap =memcacheUtils.doMapLongLongCache("reward_userticket_cache", null, null);
		boolean isCanUserTicket=true;
		if(tcacheMap!=null){
			Long time = tcacheMap.get(uin);
			if(time!=null&&time.equals(TimeTools.getToDayBeginTime())){
				isCanUserTicket=false;
			}
			logger.error("today reward cache:"+tcacheMap.size()+",uin:"+uin+",uid:"+uid+",time:"+time+",todaybegintime:"+TimeTools.getToDayBeginTime());
		}
		logger.error("choose reward ticket:uin:"+uin+",uid:"+uid+",isCanUserTicket:"+isCanUserTicket+",isAuth:"+isAuth+",total:"+total);
		
		if(isCanUserTicket){
			Double moneylimit = 9999d;//ѡȯ������
			Integer tickettype = 1;//ѡȯ����
			if(!isAuth){
				if(source == 0){
					moneylimit = 0d;
				}else if(source == 1){
					moneylimit = 0d;
				}
			}
			list = getLimitTickets(moneylimit, tickettype, uin, utype, ptype, uid, total, parkId, orderId);
		}
		return list;
	}
	
	/**
	 * ����ͣ��ȯ�������ƺ�ͣ��ȯ�������ȡͣ��ȯ�б�
	 * @param moneylimit ͣ��ȯ�������
	 * @param tickettype ͣ��ȯ��������
	 * @param uin
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @param ptype 0�˻���ֵ��1���²�Ʒ��2ͣ���ѽ��㣻3ֱ��;4���� 5����ͣ��ȯ
	 * @param uid
	 * @param total ���
	 * @return
	 */
	private List<Map<String, Object>> getLimitTickets(Double moneylimit, Integer tickettype, Long uin, Integer utype, Integer ptype, Long uid, Double total, Long parkId, Long orderId){
		Integer resource = 1;//ֻ���ù���ȯ
		if(readAllowCache(parkId)){
			logger.error("already uplimit of allowance everyday>>>uin:"+uin+",orderid:"+orderId);
			resource = 1;
		}
		List<Map<String, Object>> list = pgOnlyReadService.getAll("select * from ticket_tb where uin = ? and state=? and limit_day>=? and type<? and money<=? and resources>=?  order by money ",
				new Object[] { uin, 0, TimeTools.getToDayBeginTime(), tickettype, moneylimit, resource });
		list = chooseTicketByLevel(list, ptype, uid, total, utype, parkId, orderId);
		return list;
	}
	
	private boolean readAllowCache(Long comid){
		Double limit = memcacheUtils.readAllowLimitCacheByPark(comid);
		logger.error("comid:"+comid+",limit:"+limit);
		if(limit != null){//�л���
			Double allowmoney = memcacheUtils.readAllowCacheByPark(comid);
			logger.error("comid:"+comid+",allowmoney:"+allowmoney);
			Map<String, Object> comMap = pgOnlyReadService.getMap(
					"select allowance from com_info_tb where id=? ",
					new Object[] { comid });
			if(comMap != null && comMap.get("allowance") != null){
				Double allowance = Double.valueOf(comMap.get("allowance") + "");
				logger.error("comid:"+comid+",allowance:"+allowance);
				if(allowance > 0){
					if(allowmoney >= allowance){
						return true;
					}
				}
			}
			if(allowmoney >= limit){//�鿴�Ƿ񳬹�ÿ�ղ�������
				return true;
			}
		}else{//û�а�������������Ĳ���,��ʱ�����ܵ���������
			Double allallowmoney = memcacheUtils.readAllowanceCache();
			if(CustomDefind.getValue("ALLOWANCE") != null){
				Double uplimit = Double.valueOf(CustomDefind.getValue("ALLOWANCE") + "");
				Double toDaylimit = getAllowance(TimeTools.getToDayBeginTime(), uplimit);
//				if(toDaylimit<1000||toDaylimit>uplimit)
//					toDaylimit=1000d;
				logger.error("���ղ����ܶ� ��allallowmoney:"+allallowmoney+",uplimit:"+uplimit+",toDaylimit:"+toDaylimit);
				if(allallowmoney >= toDaylimit){//���ղ����ܶ��Ѿ�����������
					return true;
				}
			}
		}
		return false;
	}
	
	//2015-11-05 ��ʼ��ÿ���100,��0ֹͣ
	private Double getAllowance(Long time,Double limit) {
		Long baseTime = 1446652800L;//2015-11-05
		Long abs = time-baseTime;
		Long t  = abs/(24*60*60);
		logger.error(">>>>>��2015-11-03��ʼ�������ݼ�100�ı�����"+t);
		if(t>0){
			Double retDouble= limit-t*100;
			if(retDouble<0d)
				retDouble=0d;
			return retDouble;
		}
		return limit;
	}
	
	/**
	 * @param ptype 0�˻���ֵ��1���²�Ʒ��2ͣ���ѽ��㣻3ֱ��;4���� 5����ͣ��ȯ
	 * @param uid   �շ�Ա���
	 * @param total ���ѽ��
	 * @param type  0�����ݽ�����ȯ�ֿ۽�� 1������ȯ���������������ѽ���ȫ��ֿ�
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @return
	 */
	private Map<String, Object> getDistotalLimit(Integer ptype,Long uid, Double total, Integer type, Integer utype, Long orderId){
//		logger.error("getDistotalLimit>>>ptype:"+ptype+",uid:"+uid+",total:"+total+",utype:"+utype);
		Map<String, Object> map = new HashMap<String, Object>();
		Double climit = 0d;
		Double blimit = 0d;
		Double slimit = 0d;
		if(ptype == 4){//����ѡȯ
			Double rewardquota = 3.0;//�ֿ�����
			Map<String, Object> userMap = daService.getMap("select rewardquota from user_info_tb where id = ?", new Object[]{uid});
			if(userMap != null && userMap.get("rewardquota") != null){
				rewardquota =StringUtils.formatDouble(userMap.get("rewardquota"));
			}
			if(type == 0){
				if(orderId != null && orderId > 0){
					Map<String, Object> orderMap = daService.getMap("select total from order_tb where id=? ", new Object[]{orderId});
					if(orderMap != null && orderMap.get("total") != null){
						Double fee = Double.valueOf(orderMap.get("total") + "");//ͣ���ѽ��
						
						//��ͨȯ  X��֧�����ѽ���� (fee) Y������ȯ�ֿ۽�� (climit) �㷨��X=6Y-2 ������rewardquota
						climit = Math.floor((fee+2)*(1.0/6));//����ȡ��
						if(climit < 0){
							climit = 0d;
						}
						if(climit > total){
							climit = total;
						}
						if(climit > rewardquota){
							climit = rewardquota;
						}
						//����ȯ   X��֧�����ѽ���� (fee) Y������ȯ�ֿ۽�� (blimit) �㷨��X=Y������rewardquota
						blimit = Math.floor(fee);//����ȡ��
						if(blimit < 0){
							blimit = 0d;
						}
						if(blimit > total){
							blimit = total;
						}
						if(blimit > rewardquota){
							blimit = rewardquota;
						}
						//ר��ȯ   X��֧�����ѽ���� (fee) Y������ȯ�ֿ۽�� (slimit) �㷨��X=6Y-2 ������rewardquota
						slimit = Math.floor((fee+2)*(1.0/6));//����ȡ��
						if(slimit < 0){
							slimit = 0d;
						}
						if(slimit > total){
							slimit = total;
						}
						if(slimit > rewardquota){
							slimit = rewardquota;
						}
					}
				}
				logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type);
				
			}else if(type == 1){
				if(total > rewardquota){
					total = rewardquota;
				}
				//��ͨȯ  X��֧�����ѽ���� (climit) Y������ȯ�ֿ۽�� (total) �㷨��X=6Y-2 ������rewardquota
				climit = Math.ceil(total*6 - 2);
				//����ȯ  X��֧�����ѽ���� (blimit) Y������ȯ�ֿ۽�� (total) �㷨��X=Y ������rewardquota
				blimit = Math.ceil(total);
				//ר��ȯ  X��֧�����ѽ���� (slimit) Y������ȯ�ֿ۽�� (total) �㷨��X=6Y-2 ������rewardquota
				slimit = Math.ceil(total*6 - 2);
				
				map.put("distotal", total);//ʵ����ߵֿ۽��
//					logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",rewardquota:"+rewardquota+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type+",distotal:"+total);
			}
			
		}else if(ptype == -1 || ptype == 2 || ptype == 3){
			Double uplimit = 9999d;//�ֿ�����
			if(type == 0){
				//��ͨȯ  X�����ѽ���� (total) Y������ȯ�ֿ۽�� (climit) �㷨��X=6Y - 2 ������uplimit
				climit = Math.floor((total + 2)*(1.0/6));//����ȡ��
				if(climit < 0){
					climit = 0d;
				}
				if(climit > uplimit){
					climit = uplimit;
				}
				//����ȯ  X�����ѽ���� (total) Y������ȯ�ֿ۽�� (climit) �㷨��X=Y ������uplimit
				blimit = Math.floor(total);//����ȡ��
				if(blimit < 0){
					blimit = 0d;
				}
				if(blimit > uplimit){
					blimit = uplimit;
				}
				//ר��ȯ  X�����ѽ���� (total) Y������ȯ�ֿ۽�� (climit) �㷨��X=3Y+1 ������uplimit
				slimit = Math.floor((total - 1)*(1.0/3));//����ȡ��
				if(slimit < 0){
					slimit = 0d;
				}
				if(slimit > uplimit){
					slimit = uplimit;
				}
				logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",uplimit:"+uplimit+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type);
			}else if(type == 1){
				if(total > uplimit){
					total = uplimit;
				}
				//��ͨȯ  X��֧������� (climit) Y������ȯ�ֿ۽�� (total) �㷨��X=Y+1+Y/1 ������uplimit
				climit = Math.ceil(total*6 - 2);
				//����ȯ  X��֧������� (blimit) Y������ȯ�ֿ۽�� (total) �㷨��X=Y ������uplimit
				blimit = Math.ceil(total);
				//ר��ȯ  X��֧������� (slimit) Y������ȯ�ֿ۽�� (total) �㷨��X=Y+1 ������uplimit
				slimit = Math.ceil(total*3 + 1);
				map.put("distotal", total);//ʵ����ߵֿ۽��
//				logger.error("getDistotalLimit>>>uid:"+uid+",climit:"+climit+",blimit:"+blimit+",slimit:"+slimit+",uplimit:"+uplimit+",total:"+total+",ptype:"+ptype+",utype:"+utype+",type:"+type+",distotal:"+total);
			}
			
		}
		map.put("climit", climit);
		map.put("blimit", blimit);
		map.put("slimit", slimit);
//		logger.error("uid:"+uid+",map:"+map);
		setDistotalByUtype(map, utype, type);
		return map;
	}
	
	/**
	 * ��Ҫ�����Ͽͻ���utype=1������£�ȡ���ֵֿ��㷨�еֿ���С��һ����Ϊ�ֿۣ��Ͽͻ�����ѡ��ͬ��ȯ��ͬһ��limit���������Է�ֹ�û��ֶ�ѡȯʱ�ֿ۴���
	 * @param map
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @param type 0�����ݽ�����ȯ�ֿ۽�� 1������ȯ���������������ѽ���ȫ��ֿ�
	 * @return
	 */
	private Map<String, Object> setDistotalByUtype(Map<String, Object> map, Integer utype, Integer type){
//			logger.error("setDistotalByUtype>>>map:"+map+",utype:"+utype+",type:"+type);
		if(map != null && utype == 1 && type == 0){
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			for(String key : map.keySet()){
				Map<String, Object> dMap = new HashMap<String, Object>();
				dMap.put("dlimit", map.get(key));
				list.add(dMap);
			}
			//���մ�С��������
			Collections.sort(list, new ListSort6());
			Double dlimit = Double.valueOf(list.get(0).get("dlimit") + "");
//			logger.error("setDistotalByUtype>>>list:"+list+",utype:"+utype+",type:"+type);
			for(String key : map.keySet()){
				map.put(key, dlimit);
			}
//			logger.error("setDistotalByUtype>>>map:"+map+",utype:"+utype+",type:"+type);
		}
		return map;
	}
	
	public List<Map<String, Object>> getTicketInfo(List<Map<String, Object>> list, Integer ptype,Long uid, Integer utype){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Integer type=(Integer)map.get("type");
				Integer money = (Integer)map.get("money");
				Integer resources = (Integer)map.get("resources");
				Long limitDay = (Long)map.get("limit_day");
				Double backmoney = StringUtils.formatDouble(map.get("pmoney"));
				Long btime =TimeTools.getToDayBeginTime();
				//==========��ȡ������Ԫ��ȫ��ֿ�begin=============//
				Map<String, Object> fullMap = getDistotalLimit(2, uid, Double.valueOf(money + ""), 1, utype, -1L);
				Double climit = Double.valueOf(fullMap.get("climit") + "");
				Double blimit = Double.valueOf(fullMap.get("blimit") + "");
				Double slimit = Double.valueOf(fullMap.get("slimit") + "");
				Double distotal = Double.valueOf(fullMap.get("distotal") + "");
				map.put("distotal", distotal);
				if(type == 1){
					map.put("full", slimit);
				}
				if(type == 0 && resources == 0){
					map.put("full", climit);
				}
				if(type == 0 && resources == 1){
					map.put("full", blimit);
				}
				//==========��ȡ������Ԫ��ȫ��ֿ�end=============//
				if(btime >limitDay)
					map.put("exp", 0);
				else {
					map.put("exp", 1);
				}
				map.put("isbuy",resources);
				if(resources == 1){//�����ȯ
					map.put("desc", "��"+map.get("full")+"Ԫ���Եֿ�ȫ��,���ں��˻�"+backmoney+"Ԫ�������˻�");
				}else{
					map.put("desc", "��"+map.get("full")+"Ԫ���Եֿ�ȫ��");
				}
				map.put("cname", "");
				if(type == 1 && map.get("comid") != null){
					map.put("cname", getParkNameByComid((Long)map.get("comid")));
				}
				map.put("limitday", limitDay);
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param comid
	 * @return
	 */
	public String getParkNameByComid(Long comid){
		Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id =? ",new Object[]{comid});
		if(comMap!=null){
			return (String)comMap.get("company_name");
		}
		return "";		
	}
	
	/**
	 * @param list ȯ�б�
	 * @param ptype 0�˻���ֵ��1���²�Ʒ��2ͣ���ѽ��㣻3ֱ��;4���� 5����ͣ��ȯ
	 * @param uid 
	 * @param total ���ѽ��
	 * @param utype  0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @return
	 */
	public List<Map<String, Object>> chooseTicketByLevel(List<Map<String, Object>> list, Integer ptype,Long uid, Double total, Integer utype, Long parkId, Long orderId){
		//�ֿ��㷨
		Map<String, Object> distotalMap = getDistotalLimit(ptype, uid, total, 0, utype, orderId);
		Double climit = Double.valueOf(distotalMap.get("climit") + "");
		Double blimit = Double.valueOf(distotalMap.get("blimit") + "");
		Double slimit = Double.valueOf(distotalMap.get("slimit") + "");
		logger.error("the up limit of distotal>>>uid:"+uid+",map:"+distotalMap+",ptype:"+ptype+",total:"+total);
		if(list != null && !list.isEmpty()){
			for(int i=0; i<list.size();i++){
				Map<String, Object> map = list.get(i);
				Integer iscanuse = 1;//0:������ 1������
				Double limit = 0d;//��ͣ��ȯ�ɵֿ۽��
				Integer type=(Integer)map.get("type");
				Integer money = (Integer)map.get("money");
				Integer resources = (Integer)map.get("resources");
				if(type == 1){//ר��ͣ��ȯ
					if(map.get("comid") != null){
						Long comid = (Long)map.get("comid");
						if(comid.intValue() != parkId.intValue()){//���Ǹó���ר��ȯ������
							iscanuse = 0;
						}
					}else{
						iscanuse = 0;
					}
					
					if(slimit >= money){
						limit = Double.valueOf(money + "");
					}else{
						limit = slimit;
						if(utype == 0){//��ѡ��������ֿ۽���ȯ
							iscanuse = 0;
						}
					}
					map.put("limit", limit);//�ֿ۽��
					map.put("level", 3);//ר��ȯ���ȼ����
				}
				if(type == 0 && resources == 0){//�ǹ���ͣ��ȯ
					if(climit >= money){
						limit = Double.valueOf(money + "");
					}else{
						limit = climit;
						if(utype == 0){//��ѡ��������ֿ۽���ȯ
							iscanuse = 0;
						}
					}
					map.put("limit", limit);//�ֿ۽��
					map.put("level", 2);//��ͨ�ǹ���ȯ����Ȩ���
				}
				if(type == 0 && resources == 1){//����ͣ��ȯ
					if(blimit >= money){
						limit = Double.valueOf(money + "");
					}else{
						iscanuse = 0;//С��˵����ȯ����ѡ 
					}
					map.put("limit", limit);//�ֿ۽��
					map.put("level", 1);//����ȯ���ȼ����
				}
				if(limit == 0){//�ֿ�0������
					iscanuse = 0;
				}
				map.put("offset",  Math.abs(limit-money));//��ֵ����ֵ
				map.put("iscanuse", iscanuse);//�Ƿ���ô������ֿ�
			}
			Collections.sort(list, new ListSort());//����iscanuse�ɴ�С����
			Collections.sort(list, new ListSort1());//��ͬ��iscanuse���յֿ۽��limit�ɴ�С����
			Collections.sort(list, new ListSort2());//��ͬ��iscanuse��limit����offset��С��������
			Collections.sort(list, new ListSort3());//��ͬ��iscanuse��limit��offset����money��С��������
			Collections.sort(list, new ListSort4());//��ͬiscanuse��limit��offset��money����level�ɴ�С����
			Collections.sort(list, new ListSort5());//��ͬiscanuse��limit��offset��money��level��ͬ����limit_day��С��������
			
			getTicketInfo(list, ptype, uid, utype);//����ͣ��ȯ������Ԫ�ɴ����ֿ۶�
			
		}
		return list;
	}
	
	class ListSort implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			return c2.compareTo(c1);
		}
		
	}
	
	class ListSort1 implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			if(c2.compareTo(c1) == 0){
				return b2.compareTo(b1);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort2 implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0){
				return l1.compareTo(l2);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort3 implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			
			Integer m1 = (Integer)map.get("m1");
			Integer m2 = (Integer)map.get("m2");
			
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0 && l2.compareTo(l1) == 0){
				return m1.compareTo(m2);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort4 implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			
			Integer m1 = (Integer)map.get("m1");
			Integer m2 = (Integer)map.get("m2");
			
			Integer e1 = (Integer)map.get("e1");
			Integer e2 = (Integer)map.get("e2");
			
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0 && l2.compareTo(l1) == 0 && m2.compareTo(m1) == 0){
				return e2.compareTo(e1);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort5 implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			Map map = getParams(o1, o2);
			Integer c1 = (Integer)map.get("c1");
			Integer c2 = (Integer)map.get("c2");
			
			BigDecimal b1 = (BigDecimal)map.get("b1");
			BigDecimal b2 = (BigDecimal)map.get("b2");
			
			BigDecimal l1 = (BigDecimal)map.get("l1");
			BigDecimal l2 = (BigDecimal)map.get("l2");
			
			Integer m1 = (Integer)map.get("m1");
			Integer m2 = (Integer)map.get("m2");
			
			Integer e1 = (Integer)map.get("e1");
			Integer e2 = (Integer)map.get("e2");
			
			Long d1 = (Long)map.get("d1");
			Long d2 = (Long)map.get("d2");
			
			if(c2.compareTo(c1) == 0 && b2.compareTo(b1) == 0 && l2.compareTo(l1) == 0 && m2.compareTo(m1) == 0 && e2.compareTo(e1) == 0){
				return d1.compareTo(d2);
			}else{
				return 0;
			}
		}
		
	}
	
	class ListSort6 implements Comparator<Map<String, Object>>{
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			BigDecimal b1 = new BigDecimal(0);
			BigDecimal b2 = new BigDecimal(0);
			if(o1.get("dlimit") != null){
				if(o1.get("dlimit") instanceof Double){
					Double ctotal = (Double)o1.get("dlimit");
					b1 = b1.valueOf(ctotal);
				}else{
					b1 = (BigDecimal)o1.get("dlimit");
				}
			}
			if(o2.get("dlimit") != null){
				if(o2.get("dlimit") instanceof Double){
					Double ctotal = (Double)o2.get("dlimit");
					b2 = b2.valueOf(ctotal);
				}else{
					b2 = (BigDecimal)o2.get("dlimit");
				}
			}
			return b1.compareTo(b2);
		}
		
	}
	
	public List<Map<String, Object>> getCarType(Long comid){
		Map<String, Object> map = daService.getMap("select car_type from com_info_tb where id=? ", new Object[]{comid});
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		
		if(map != null){
			Integer car_type = (Integer)map.get("car_type");
			if(car_type != 0){
				List<Map<String, Object>> list = pgOnlyReadService.getAll("select id as value_no,name as value_name from car_type_tb where comid=? order by sort , id desc ", new Object[]{comid});
				if(!list.isEmpty()){
					result.addAll(list);
				}else {
					Map<String, Object> bigMap = new HashMap<String, Object>();
					bigMap.put("value_name","С��");
					bigMap.put("value_no", 1);
					Map<String, Object> smallMap = new HashMap<String, Object>();
					smallMap.put("value_name","��");
					smallMap.put("value_no", 2);
					result.add(bigMap);
					result.add(smallMap);
				}
			}else {
				Map<String, Object> firtstMap = new HashMap<String, Object>();
				firtstMap.put("value_name","ͨ��");
				firtstMap.put("value_no", 0);
				result.add(firtstMap);
			}
		}
		return result;
	}
	private Map<String, Object> getParams(Map<String, Object> o1, Map<String, Object> o2){
		Map<String, Object> map = new HashMap<String, Object>();
		Integer c1 = (Integer)o1.get("iscanuse");
		if(c1 == null) c1 = 0;
		Integer c2 = (Integer)o2.get("iscanuse");
		if(c2 == null) c2 = 0;
		map.put("c1", c1);
		map.put("c2", c2);
		
		BigDecimal b1 = new BigDecimal(0);
		BigDecimal b2 = new BigDecimal(0);
		if(o1.get("limit") != null){
			if(o1.get("limit") instanceof Double){
				Double ctotal = (Double)o1.get("limit");
				b1 = b1.valueOf(ctotal);
			}else{
				b1 = (BigDecimal)o1.get("limit");
			}
		}
		if(o2.get("limit") != null){
			if(o2.get("limit") instanceof Double){
				Double ctotal = (Double)o2.get("limit");
				b2 = b2.valueOf(ctotal);
			}else{
				b2 = (BigDecimal)o2.get("limit");
			}
		}
		map.put("b1", b1);
		map.put("b2", b2);

		BigDecimal l1 = new BigDecimal(0);
		BigDecimal l2 = new BigDecimal(0);
		if(o1.get("offset") != null){
			if(o1.get("offset") instanceof Double){
				Double ctotal = (Double)o1.get("offset");
				l1 = l1.valueOf(ctotal);
			}else{
				l1 = (BigDecimal)o1.get("offset");
			}
		}
		if(o2.get("offset") != null){
			if(o2.get("offset") instanceof Double){
				Double ctotal = (Double)o2.get("offset");
				l2 = l2.valueOf(ctotal);
			}else{
				l2 = (BigDecimal)o2.get("offset");
			}
		}
		map.put("l1", l1);
		map.put("l2", l2);
		
		Integer m1 = (Integer)o1.get("money");
		if(m1 == null) m1 = 0;
		Integer m2 = (Integer)o2.get("money");
		if(m2 == null) m2 = 0;
		map.put("m1", m1);
		map.put("m2", m2);
		
		Integer e1 = (Integer)o1.get("level");
		if(e1 == null) e1 = 0;
		Integer e2 = (Integer)o2.get("level");
		if(e2 == null) e2 = 0;
		map.put("e1", e1);
		map.put("e2", e2);
		
		Long d1 = (Long)o1.get("limit_day");
		if(d1 == null) d1 = 0L;
		Long d2 = (Long)o2.get("limit_day");
		if(d2 == null) d2 = 0L;
		map.put("d1", d1);
		map.put("d2", d2);
		
		return map;
	}
	//----------------ѡȯ�߼�end--------------------//
}