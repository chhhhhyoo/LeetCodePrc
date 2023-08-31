package com.epopcon.wspider.taskJob.parsing.category.ssg.traverse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.epopcon.eums.exception.PException;
import com.epopcon.wspider.common.code.WSCode;
import com.epopcon.wspider.common.code.WSErrorCode;
import com.epopcon.wspider.common.data.extract.Category;
import com.epopcon.wspider.common.logger.Logger;
import com.epopcon.wspider.common.network.http.Get;
import com.epopcon.wspider.common.network.http.Header;
import com.epopcon.wspider.common.traverse.Leaf;
import com.epopcon.wspider.common.traverse.LeafTraverse;
import com.epopcon.wspider.common.util.URLUtils;
import com.epopcon.wspider.logger.WorkUnit;
import com.epopcon.wspider.net.HttpRequestService;
import com.epopcon.wspider.net.Result;
import com.epopcon.wspider.net.access.AccessDenied;
import com.epopcon.wspider.scrap.AbstractScrapContentValidator.SCRAP_VAR;
import com.epopcon.wspider.scrap.ScrapContentValidator;
import com.epopcon.wspider.service.filter.ParseFilterList;
import com.epopcon.wspider.service.task.WspiderUnitLogger;
import com.epopcon.wspider.task.fashion.items.FashionItemCategory;
import com.epopcon.wspider.taskJob.parsing.cv.ssg.SsgCV;
import com.mysql.jdbc.StringUtils;

/**
 * <pre>
 * com.epopcon.wspider.taskJob.fashion.ssg 
 * 
 * </pre>
 * @date : 2018. 1. 31. 오후 3:10:01
 * @version : 
 * @author : 김상은
 */

public class SsgCategory extends FashionItemCategory implements AccessDenied {
	public final int PARSING_FLAG_REQ_CATEGORY = 1;
	private final int PARSING_FLAG_JSON_CATEGORY = 11;
	private final int PARSING_FLAG_REQ_SUB_CATEGORY = 2;
	private final int PARSING_FLAG_REQ_SUB_TWO_CATEGORY = 3;
	
	private final String SSGCODE = "6005";
	private final String BASEURL = "http://www.ssg.com/disp/category.ssg?ctgId=";
	
	private final String BASEURL2 = "http://www.ssg.com/disp/category.ssg?";
	

	public SsgCategory(Long jobId, HttpRequestService request, ParseFilterList pFilterList, LeafTraverse lft,ScrapContentValidator ssgscrapValidator,Logger logger ,WspiderUnitLogger workUnitLogger) {
		super(jobId, request, pFilterList, lft, ssgscrapValidator , logger, workUnitLogger);
	}
	@Override
	public void parse(int parsingFlag, Map<String, Object> parsingParam, Result result) throws Exception {
		int responseCode = result.getResponseCode();
		if (responseCode == HttpStatus.SC_OK) {
			if(parsingFlag == PARSING_FLAG_REQ_CATEGORY) {
				parseCatetory(parsingParam, result);
			} else if(parsingFlag == PARSING_FLAG_JSON_CATEGORY) { // category json data
				parseJsonCatetory(parsingParam, result);
			} else if(parsingFlag == PARSING_FLAG_REQ_SUB_CATEGORY) {
				parseSubCatetory(parsingParam, result);
			} else if(parsingFlag == PARSING_FLAG_REQ_SUB_TWO_CATEGORY) {
				parseSubTwoCatetory(parsingParam, result);
			}
		}
		else {	
			throw PException.buildException(WSErrorCode.HTTP_REQUEST_FAIL, "Http Request Fail!! ResponseCode -> {}", responseCode);
		}
	}

	/**
	 * 
	 * <pre>
	 *  Description : 카테고리 파싱 중 메인페이지에서의 파싱
	 * </pre>
	 * @Method Name : parseCatetory
	 * @date : 2018. 1. 31.
	 * @author : 김상은
	 * @history : 
	 *	-----------------------------------------------------------------------
	 *	Date				Writer						Content  
	 *	----------- ------------------- ---------------------------------------
	 *	2018. 1. 31.		김상은				Initial Write
	 *	-----------------------------------------------------------------------
	 * 
	 * @param param
	 * @param result
	 * @throws IOException
	 */
	private void parseCatetory(Map<String, Object> param, Result result) throws IOException {
		WorkUnit wu = (WorkUnit) param.get(WSCode.CURRENT_WORK_UNIT);
		String content = result.getPageContent();
		wu.addJobResult("content", content);
		
		Get get = Get.make(SsgCV.JSONJS); // 모든 카테고리의 정보가 있는 곳.
		Header header = new Header();
		header.addParam("Referrer Policy", "no-referrer-when-downgrade");
		header.addParam("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		header.addParam("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");

		request(null, PARSING_FLAG_JSON_CATEGORY, param, header, get, this,
				WSCode.WORK_TYPE_FASHION_SEARCH_CATE_ONE_DEPTH);

		result.putObject("category", lft.toCategoryList());
	}
	
	/**
	 * 
	 * <pre>
	 *  Description : 메인페이지 이후 request가 필요한 상황일 때 사용 ( 메인 카테고리)
	 * </pre>
	 * @Method Name : getSubCategories
	 * @date : 2018. 1. 31.
	 * @author : 김상은
	 * @history : 
	 *	-----------------------------------------------------------------------
	 *	Date				Writer						Content  
	 *	----------- ------------------- ---------------------------------------
	 *	2018. 1. 31.		김상은				Initial Write
	 *	-----------------------------------------------------------------------
	 * 
	 * @param param
	 */
	
	private void getSubCategories(Map<String, Object> param) {
		List<Leaf> firstLeaves = lft.getFirstDepthChildLeaves();
		for (Leaf first : firstLeaves) {
			List<Leaf> secondLeaves = first.getChildLeaf();
			if (secondLeaves != null) {
				for (Leaf second : secondLeaves) {
					List<Leaf> thirdLeaves = second.getChildLeaf();
					if (thirdLeaves != null) {
						for (Leaf third : thirdLeaves) {
							String url = third.getUrl();
							url = URLUtils.remove(url);
							if (!StringUtils.isNullOrEmpty(url)) {
								Get get = Get.make(url);
								Header header = new Header();
								request(third, PARSING_FLAG_REQ_SUB_CATEGORY, param, header, get, this,
										WSCode.WORK_TYPE_FASHION_SEARCH_CATE_ONE_DEPTH);
							}
						}

					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 * <pre>
	 *  Description : request 후 카테고리 파싱이 필요한 경우 사용한다. (베스트 , 기획전 카테고리)
	 * </pre>
	 * @Method Name : getSubCategories
	 * @date : 2018. 1. 31.
	 * @author : 김상은
	 * @history : 
	 *	-----------------------------------------------------------------------
	 *	Date				Writer						Content  
	 *	----------- ------------------- ---------------------------------------
	 *	2018. 1. 31.		김상은				Initial Write
	 *	-----------------------------------------------------------------------
	 * 
	 * @param param
	 * @param leaf
	 * @param flag
	 * @param wscode
	 */
	private void getSubCategories(Map<String, Object> param, Leaf leaf ,int flag , String wscode) {
		if(leaf!=null){
			Get get  = Get.make(leaf.getUrl());
			Header header = new Header();								
			request(leaf, flag, param, header, get, this,wscode);
		}
	}
	

	
	
	private void parseJsonCatetory(Map<String, Object> param, Result result) throws IOException, ParseException {
		String content = result.getPageContent();
		
		content = content.replaceAll(".$", "").substring(content.indexOf("["));
		
		JSONParser parser = new JSONParser();
		JSONArray jsonarr = (JSONArray) parser.parse(content);
		for(Object ob1 : jsonarr){
			JSONObject ob = (JSONObject) ob1;
			if(ob.get("S").equals(SSGCODE)){
				JSONObject a=(JSONObject)ob.get("L");
				JSONArray arrtemp=(JSONArray) a.get("D");
				for(Object first: arrtemp){
					JSONObject firstOb=(JSONObject)first;
					String firstname = firstOb.get("N")+"";
					if(logger.isDebugEnabled())
						logger.debug(getClass().getSimpleName(), "category traverse 1depth cate name : " + firstname);
					String firsturl = "";
					Leaf firstLeaf = addChildLeaf(null, firstname, firsturl);
					if(firstLeaf!=null){
						//======================================================================================================================
						//====================validate category-1 content=========================================================================
						Map<SCRAP_VAR,Object> validateParam = new HashMap<>();
						validateParam.put(SCRAP_VAR.CATE_DEPT_1, firstname);
						validateParam.put(SCRAP_VAR.URL, result.getRequestURI().toURL().toString());
						scrapValidator.validateContentForCategory1(validateParam);
						//======================================================================================================================						
						JSONArray secondarr =(JSONArray) firstOb.get("L");
						for(Object second : secondarr){
							JSONObject secondOb =(JSONObject) second;
							String secondname = secondOb.get("N")+"";
							String secondurl = secondOb.get("C")+"";
							secondurl = BASEURL+secondurl;
							Leaf secondLeaf = addChildLeaf(firstLeaf, secondname, secondurl);
							if(secondLeaf!=null){
								//========================================================================================================================
								//====================validate category-2 content=========================================================================
								validateParam.clear();
								validateParam.put(SCRAP_VAR.CATE_DEPT_2, secondname);
								validateParam.put(SCRAP_VAR.LEAF, firstLeaf);
								validateParam.put(SCRAP_VAR.URL, result.getRequestURI().toURL().toString());
								scrapValidator.validateContentForCategory2(validateParam);
								//========================================================================================================================
								for(Object third : (JSONArray) secondOb.get("L")){
									JSONObject thirdOb =(JSONObject) third;
									String thirdname = thirdOb.get("N")+"";
									String thirdurl = thirdOb.get("C")+"";
									thirdurl = BASEURL+thirdurl;
									Leaf thirdLeaf = addChildLeaf(secondLeaf, thirdname, thirdurl);
									if(thirdLeaf!=null){
										if(thirdname.equals("자동차용품")){
											addChildLeaf(secondLeaf, thirdname, thirdurl);
											//========================================================================================================================
											//====================validate category-3 content=========================================================================
											validateParam.clear();
											validateParam.put(SCRAP_VAR.CATE_DEPT_3, thirdname);
											validateParam.put(SCRAP_VAR.LEAF, secondLeaf);
											validateParam.put(SCRAP_VAR.URL, result.getRequestURI().toURL().toString());
											scrapValidator.validateContentForCategory3(validateParam);
											//========================================================================================================================
											for(Object fourth : (JSONArray) thirdOb.get("L")){
												JSONObject fourthOb =(JSONObject) fourth;
												String fourthname = fourthOb.get("N")+"";
												String fourthurl = BASEURL+fourthOb.get("C")+"";
												if(fourthname.equals("타이어"))
													addChildLeaf(thirdLeaf, fourthname, fourthurl);
											}
										}else if(parsingMore(thirdname) && !secondname.contains("바디케어")){
											//==============================================대상 최하위 뎁스까지==================================================
											validateParam.clear();
											validateParam.put(SCRAP_VAR.CATE_DEPT_3, thirdname);
											validateParam.put(SCRAP_VAR.LEAF, secondLeaf);
											validateParam.put(SCRAP_VAR.URL, result.getRequestURI().toURL().toString());
											scrapValidator.validateContentForCategory3(validateParam);
											for(Object fourth : (JSONArray) thirdOb.get("L")){
												JSONObject fourthOb =(JSONObject) fourth;
												String fourthname = fourthOb.get("N")+"";
												String fourthurl = BASEURL+fourthOb.get("C")+"";
												addChildLeaf(thirdLeaf, fourthname, fourthurl);
											}
										}
									}
								}
							}
							
						}
					}
				}
			}
		}
//		getSubCategories(param); //들어간 후 다시 카테고리를 파싱한다.
	}
	private boolean parsingMore(String cateName) {
		List<String> depthFilter = new ArrayList<String>();
		depthFilter.add("김치");
		depthFilter.add("반찬/젓갈");
		depthFilter.add("옥수수/피클/과일통조림");
		depthFilter.add("참치/스팸/축수산통조림");
		
		depthFilter.add("청소기/다리미");
		depthFilter.add("전화기/재봉틀/무전기");
		depthFilter.add("비데/미용/기타가전");
		depthFilter.add("해외직구");

		depthFilter.add("노트북/테블릿PC");
		depthFilter.add("데스크탑");
		depthFilter.add("모니터");

		depthFilter.add("디지털카메라/캠코더");
		depthFilter.add("카메라렌즈");
		depthFilter.add("청소/보관용품");
		depthFilter.add("오디오/카세트");
		depthFilter.add("TV/영상가전");
		//20220516가전디지털 filter추가
		depthFilter.add("전기밥솥/레인지");
		depthFilter.add("식기세척/음식물처리기");
		depthFilter.add("주방소형가전");
		depthFilter.add("커피머신/용품");
		depthFilter.add("냉장고/김치냉장고");
		depthFilter.add("세탁기/건조기");
		depthFilter.add("업소용가전");
		depthFilter.add("에어컨");
		depthFilter.add("선풍기/냉풍기");
		depthFilter.add("제습기/공기청정기");
		depthFilter.add("난방용품");
		
		depthFilter.add("화장지");
		depthFilter.add("물티슈");
		depthFilter.add("미용티슈");
		depthFilter.add("생리대");
		depthFilter.add("일반생리대");
		depthFilter.add("성인기저귀");

		depthFilter.add("세탁세제");
		depthFilter.add("주방/청소세제");
		depthFilter.add("탈취/방향제");
		depthFilter.add("살충제/방충제");
		depthFilter.add("개미/바퀴퇴치제");
		depthFilter.add("세제선물세트");

		depthFilter.add("마스크");
		depthFilter.add("칫솔/치약/구강청결");
		depthFilter.add("면도/제모용품");
		depthFilter.add("안마의자/마사지용품");

		depthFilter.add("세면기/변기/비데");

		depthFilter.add("기저귀/물티슈");

		depthFilter.add("일반분유");
		depthFilter.add("산양분유");
		depthFilter.add("특수/기타분유");

		depthFilter.add("스킨/바디케어");
		depthFilter.add("구강용품");
		depthFilter.add("세탁용품");

		depthFilter.add("기저귀/물티슈");
		depthFilter.add("분유/유아식");
		depthFilter.add("출산/육아용품");

		pFilterList.addFilter("일반전화기");
		pFilterList.addFilter("유무선전화기");
		pFilterList.addFilter("무전기");
		pFilterList.addFilter("재봉틀");
		pFilterList.addFilter("정수기");
		pFilterList.addFilter("가전/디지털 직구");
		pFilterList.addFilter("리빙/인테리어 직구");
		pFilterList.addFilter("모니터보안기");
		pFilterList.addFilter("모니터액세서리");
		pFilterList.addFilter("보관함/제습용품");
		pFilterList.addFilter("청소도구/LCD용품/소프트버튼");
		pFilterList.addFilter("CDP, 카세트, 라디오, 턴테이블");
		pFilterList.addFilter("영상주변기기");
		pFilterList.addFilter("사이니지");
		pFilterList.addFilter("빨래솔");
		pFilterList.addFilter("빨래판");
		pFilterList.addFilter("세탁망");
		pFilterList.addFilter("빨래건조대");
		pFilterList.addFilter("다리미판");
		pFilterList.addFilter("빨래바구니");
		pFilterList.addFilter("분무기/세탁보조용품");
		pFilterList.addFilter("옷걸이/후크");
		pFilterList.addFilter("마스크 스트랩");
		pFilterList.addFilter("마스크 보관함");
		pFilterList.addFilter("세면대");
		pFilterList.addFilter("변기");
		boolean isFilter = isCollectCate(depthFilter, cateName);
		return isFilter;
	}
	private boolean isCollectCate(List<String> list, String cateName){
		// true면 수집 
		return list.stream().filter(f ->{
									 if(f.matches(cateName)) return true;
									 return false;
										}).findFirst().isPresent();
		
	}
	/**
	 * 
	 * <pre>
	 *  Description : request 후 파싱
	 * </pre>
	 * @Method Name : parseSubCatetory
	 * @date : 2019. 7. 3.
	 * @author : 김상은
	 * @history : 
	 *	-----------------------------------------------------------------------
	 *	Date				Writer						Content  
	 *	----------- ------------------- ---------------------------------------
	 *	2019. 7. 3.		김상은				Initial Write
	 *	-----------------------------------------------------------------------
	 * 
	 * @param param
	 * @param result
	 * @throws IOException
	 */
	private void parseSubCatetory(Map<String, Object> param, Result result) throws IOException {
		Leaf leaf = (Leaf) param.get(WSCode.CURRENT_LEAF); //이전 카테고리에 이어서 Leaf를 이어나간다.
		
		String content = result.getPageContent();

		Document doc = Jsoup.parse(content);
		
		Elements cate = doc.select("div.cmflt_filbox_cts > div.cmfltExpContent >ul > li");
		
		for(Element e1 : cate){
			String name = e1.select("a").text();
			String qStringType = e1.select("a").attr("data-ilparam-type");
			String qStringValue = e1.select("a").attr("data-ilparam-value");
			
			String url = BASEURL2+String.format("%s=%s", qStringType,qStringValue);
			
			//======================================================================================================================
			//====================validate category-3 content=========================================================================
			Map<SCRAP_VAR,Object> validateParam = new HashMap<>();
			validateParam.put(SCRAP_VAR.CATE_DEPT_3, cate);
			validateParam.put(SCRAP_VAR.LEAF, leaf);
			validateParam.put(SCRAP_VAR.URL, url);
			scrapValidator.validateContentForCategory3(validateParam);
			//======================================================================================================================
			Leaf leaf2 = addChildLeaf(leaf, name, url);
//			getSubCategories(param,leaf2, PARSING_FLAG_REQ_SUB_TWO_CATEGORY, WSCode.WORK_TYPE_FASHION_SEARCH_CATE_THREE_DEPTH);
		}
	}
/**
 * 
 * <pre>
 *  Description : 신규 카테고리 파싱 최종 request 후 파싱 
 * </pre>
 * @Method Name : parseSubTwoCatetory
 * @date : 2019. 7. 3.
 * @author : 김상은
 * @history : 
 *	-----------------------------------------------------------------------
 *	Date				Writer						Content  
 *	----------- ------------------- ---------------------------------------
 *	2019. 7. 3.		김상은				Initial Write
 *	-----------------------------------------------------------------------
 * 
 * @param param
 * @param result
 * @throws IOException
 */
	private void parseSubTwoCatetory(Map<String, Object> param, Result result) throws IOException {
		Leaf leaf = (Leaf) param.get(WSCode.CURRENT_LEAF);
		
		String content = result.getPageContent();

		Document doc = Jsoup.parse(content);
		
		Elements cate = doc.select("div.cmflt_filbox_cts >ul.cmflt_ctlist > li");
		
		for(Element e1 : cate){
			String name = e1.select("a").text();
			
			String qStringType = e1.select("a").attr("data-ilparam-type");
			String qStringValue = e1.select("a").attr("data-ilparam-value");
			
			String url = BASEURL2+String.format("%s=%s", qStringType,qStringValue);
			
			logger.debug(getClass().getSimpleName(), "name : " + name +" , url : "+ url);
			
			//======================================================================================================================
			//====================validate category-4 content=========================================================================
			Map<SCRAP_VAR,Object> validateParam = new HashMap<>();
			validateParam.put(SCRAP_VAR.CATE_DEPT_4, cate);
			validateParam.put(SCRAP_VAR.LEAF, leaf);
			validateParam.put(SCRAP_VAR.URL, url);
			scrapValidator.validateContentForCategory4(validateParam);
			//======================================================================================================================
			
			addChildLeaf(leaf, name, url);
		}
		
	}
	
	/**
	 * 
	 * <pre>
	 *  Description : 상위 첫번째 카테고리이름과 비교하여 같은것만 return 하는 메소드.
	 * </pre>
	 * @Method Name : checktraverse
	 * @date : 2018. 1. 31.
	 * @author : 김상은
	 * @history : 
	 *	-----------------------------------------------------------------------
	 *	Date				Writer						Content  
	 *	----------- ------------------- ---------------------------------------
	 *	2018. 1. 31.		김상은				Initial Write
	 *	-----------------------------------------------------------------------
	 * 
	 * @param cateName
	 * 			상위 이름과 비교할 문자열
	 * @return
	 */
	public boolean checktraverse(String cateName){
		boolean check=false;
		List<Leaf> firstLeaves = lft.getFirstDepthChildLeaves();
		for(Leaf first : firstLeaves) {
			if(first.getName().equals(cateName)){
				check=true;
			}		
		}
		return check;
	}
	
	public List<Category> getCategory() {
		return lft.toCategoryList();
	}

	/**
	 * 하위 Leaf를 생성한다.
	 * 
	 * @param leaf
	 *            상위 Leaf
	 * @param name
	 *            생성할 Leaf 이름
	 * @param url
	 *            생성할 Leaf Url
	 * @return Leaf
	 */
	private Leaf addChildLeaf(Leaf leaf, String name, String url) {
		return addChildLeaf(leaf, name, url, false);
	}

	/**
	 * 하위 Leaf를 생성한다.
	 * 
	 * @param leaf
	 *            상위 Leaf
	 * @param name
	 *            생성할 Leaf 이름
	 * @param url
	 *            생성할 Leaf Url
	 * @param isBest
	 *            true인 경우 Best Item 있는 Category
	 * @return Leaf
	 */
	private Leaf addChildLeaf(Leaf leaf, String name, String url, boolean isBest) {
		return addChildLeaf(null, leaf, name, url, isBest);
	}

	/**
	 * 하위 Leaf를 생성한다.
	 * 
	 * @param doc
	 *            기획전이 있는 페이지인지 확인하기 위한 Document
	 * @param leaf
	 *            상위 Leaf
	 * @param name
	 *            생성할 Leaf 이름
	 * @param url
	 *            생성할 Leaf Url
	 * @param isBest
	 *            true인 경우 Best Item 있는 Category
	 * @return Leaf
	 */
	private Leaf addChildLeaf(Document doc, Leaf leaf, String name, String url, boolean isBest) {
		if (name == null || name.trim().length() == 0 || pFilterList.filter(name, leaf)) {
			return null;
		}

		boolean isSpecial = false;

		Leaf childLeaf = Leaf.make(name.trim(), url != null ? url.trim() : url, isBest, isSpecial);
		if (leaf != null) {
			leaf.addChildLeaf(childLeaf, lft);
		} else {
			lft.addChildLeaf(childLeaf);
		}
		return childLeaf;
	}
	

	@Override
	public boolean checkAccessDenined(int reponseCode, String content, String url, AtomicInteger sleepPeriod,
			AtomicBoolean cacheCleanYn) {
		return false;
	}
	@Override
	public boolean checkAccessRestricted(int reponseCode, String content, String url, AtomicInteger sleepPeriod,
			AtomicBoolean cacheCleanYn) {
		if(reponseCode==401){
			cacheCleanYn.set(true);
//			sleepPeriod.set(60000);
			return true;
		}
		return false;
	}

}------------------------------------
package com.epopcon.wspider.taskJob.parsing.detail.ssg;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.epopcon.eums.exception.PException;
import com.epopcon.wspider.common.code.WSCode;
import com.epopcon.wspider.common.code.WSErrorCode;
import com.epopcon.wspider.common.data.CommentItem;
import com.epopcon.wspider.common.data.Item;
import com.epopcon.wspider.common.logger.Logger;
import com.epopcon.wspider.common.match.MatchUtils;
import com.epopcon.wspider.common.network.http.Get;
import com.epopcon.wspider.common.network.http.Header;
import com.epopcon.wspider.common.network.http.Post;
import com.epopcon.wspider.common.util.DateUtils;
import com.epopcon.wspider.common.util.GoodNumUtils;
import com.epopcon.wspider.common.util.StringUtils;
import com.epopcon.wspider.common.util.URLUtils;
import com.epopcon.wspider.db.fashion.dto.ColtBaseUrlItem;
import com.epopcon.wspider.db.fashion.dto.ColtItem;
import com.epopcon.wspider.db.fashion.dto.ColtItemEvalut;
import com.epopcon.wspider.db.fashion.dto.ColtItemIvt;
import com.epopcon.wspider.db.fashion.dto.ColtRetryItem;
import com.epopcon.wspider.db.fashion.dto.ColtTghtSellItem;
import com.epopcon.wspider.net.HttpRequestService;
import com.epopcon.wspider.net.Result;
import com.epopcon.wspider.net.access.AccessDenied;
import com.epopcon.wspider.process.SubProcessExecutor;
import com.epopcon.wspider.scrap.ScrapContentValidator;
import com.epopcon.wspider.service.task.WspiderExtract;
import com.epopcon.wspider.service.task.WspiderUnitLogger;
import com.epopcon.wspider.task.data.DataReduction;
import com.epopcon.wspider.task.fashion.items.FashionItemDetail;
import com.epopcon.wspider.taskJob.fashion.brand.FashionBrandRepository;
import com.epopcon.wspider.taskJob.parsing.cv.ssg.SsgCV;
import com.epopcon.wspider.taskJob.parsing.scrap.ssg.SsgScrapValidator;

/**
 * <pre>
 * com.epopcon.wspider.taskJob.fashion.ssg
 * 
 * </pre>
 * 
 * @date : 2018. 1. 31. 오후 3:10:01
 * @version :
 * @author : 김상은
 */

public class SsgItemDetail extends FashionItemDetail implements AccessDenied {
    private FashionBrandRepository fashionBrandRepo;
    private DataReduction dataReductionHandler;
    private SubProcessExecutor processExec;

    private String COLL_SITE = "top.ssg.com";

    public final String INIT_URL = "http://www.ssg.com/";

    public final String PATTERN_01 = "uitemObj\\s\\=\\s\\{(.*?)\\};";
    public final String PATTERN_01_1 = "uitemObjArr\\.push\\(uitemObj\\);";

    public final String PATTERN_02 = "salestrUitemList\\s\\=\\s\\{(.*?)\\};";
    public final String PATTERN_02_1 = "resultItemObj\\.salestrUitemList\\.push\\(salestrUitemList\\);";

    public final String PATTERN_03 = "\\t(itemObj)\\s\\=\\s\\{(.*?)\\};"; // 번들
                                                                          // 아이템.
    public final String PATTERN_03_1 = "map.put\\(.ITEM"; // 번들 아이템.

    public final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";

    public final String plusurl = "http://www.ssg.com/item/itemView.ssg?itemId=";

    protected ColtBaseUrlItem baseUrlItem;

    protected String category;

    private int collectDataInterval = 60; // default 60 minute

    private boolean isEvalut = false; // 기본값은 수집 안한다.

    public void craEvalut() { // 리뷰수집변경
        this.isEvalut = true;
    }

    private boolean promotion = false;

    public void setPromotion() {
        this.promotion = true;
    }

    private String siteName = SsgCV.SITE_NAME;

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public SsgItemDetail(WspiderExtract extract, Logger logger, SubProcessExecutor processExec,
            DataReduction dataReductionHandler,
            FashionBrandRepository fashionBrandRepo, File orgUrlDirectory, HttpRequestService request,
            WspiderUnitLogger workUnitLogger, ScrapContentValidator scrapValidator, String category,
            ColtBaseUrlItem baseUrlItem, Integer total, Integer seq, String COLL_SITE, int collectDateInterval) {
        super(extract, logger, orgUrlDirectory, request, workUnitLogger, scrapValidator, category, total, seq);
        this.COLL_SITE = COLL_SITE;
        this.collectDataInterval = collectDateInterval;
        this.fashionBrandRepo = fashionBrandRepo;
        this.dataReductionHandler = dataReductionHandler;
        this.processExec = processExec;
        this.baseUrlItem = baseUrlItem;
        this.category = category;
    }

    protected SsgItemDetail(Logger logger, SubProcessExecutor processExec, DataReduction dataReductionHandler,
            FashionBrandRepository fashionBrandRepo, File orgUrlDirectory, HttpRequestService request,
            WspiderUnitLogger workUnitLogger, ScrapContentValidator scrapValidator, String category,
            ColtBaseUrlItem baseUrlItem, Integer total, Integer seq, String COLL_SITE, int collectDateInterval) {
        super(logger, orgUrlDirectory, request, workUnitLogger, scrapValidator, category, total, seq);
        this.COLL_SITE = COLL_SITE;
        this.collectDataInterval = collectDateInterval;
        this.fashionBrandRepo = fashionBrandRepo;
        this.dataReductionHandler = dataReductionHandler;
        this.processExec = processExec;
        this.baseUrlItem = baseUrlItem;
        this.category = category;
    }

    @Override
    public Item parseItemDetail(Long jobId, URI uri, String collectSite, String content) throws PException {
        Item item = parseItem(jobId, uri, collectSite, content);

        if (item instanceof ColtItem) {
            boolean stockYn = ((ColtItem) item).isNotColtItemIvtEmpty();
            if (!stockYn) {
                logger.error(getClass().getSimpleName(),
                        String.format("#stock empty -> %s Collecturl %s ", DateUtils.getToday(), uri.toString()));
                return null;
            }
        } else if (item instanceof ColtTghtSellItem) {
            List<ColtItem> coltItemList = ((ColtTghtSellItem) item).getBundleItems();
            if (coltItemList != null && !coltItemList.isEmpty()) {
                for (ColtItem cItem : coltItemList) {
                    boolean stockYn = ((ColtItem) cItem).isNotColtItemIvtEmpty();
                    if (!stockYn) {
                        logger.error(getClass().getSimpleName(), String.format("#stock empty -> %s Collecturl %s ",
                                DateUtils.getToday(), uri.toString()));
                        return null;
                    }
                }
            } else {
                logger.error(getClass().getSimpleName(),
                        String.format("#coltItems empty -> %s Collecturl %s ", DateUtils.getToday(), uri.toString()));
                return null;
            }
        }
        return item;
    }

    /**
     * 
     * <pre>
     *  Description : 번들 아이템은 개별 아이템의 묶음 이기 때문에 번들아이템 페이지에서 itemNum 을 가져와 개별 아이템으로 try 한다.
     * </pre>
     * 
     * @Method Name : parseBundleSellingItem
     * @date : 2018. 2. 19.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 2. 19. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param jobId
     * @param uri
     * @param collectSite
     * @param content
     * @param doc
     * @return
     */
    protected Item parseBundleSellingItem(Long jobId, URI uri, String collectSite, String content, Document doc) {
        if (doc == null) {
            doc = Jsoup.parse(content);
        }
        // 번들아이템으로 설정된것들은 전부 개별아이템으로 카테고리에 파싱이 된다.
        //
        String no = getItemNum(uri.toString());
        String title = getBundleTitle(doc);

        ColtTghtSellItem sellItem = ColtTghtSellItem.make(no, title, collectSite);
        try {

            List<String> bundle = bundleItemNum(doc);
            bundle = new ArrayList<String>(new HashSet<String>(bundle));
            for (String url : bundle) {
                Get get = Get.make(url);
                Header header = SsgCV.makeHeader(uri.toString());
                Result result = requestSizeGuide(jobId, header, get, this, null);
                URI subURI = result.getRequestURI();
                String bundleContent = result.getPageContent("UTF-8");

                Item item = parseItem(jobId, subURI, collectSite, bundleContent);

                if (item == null) {
                    continue;
                }
                ColtItem citem = (ColtItem) item;

                if (logger.isDebugEnabled()) {
                    logger.debug(getClass().getSimpleName(), String
                            .format("ColtTghtSellItem-> %s, Now working... coltItem -> %s", no, citem.getItemNum()));
                }

                sellItem.addColtItem(citem);
                sellItem.addColtTghtRelItem(citem.getGoodsName(), citem.getItemNum(), collectSite);
            }
            getSiteSellHis(sellItem, doc);

            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(), String
                        .format("ColtTghtSellItem Count-> %s", bundle.size()));
            }

            return sellItem;
        } catch (Exception e) {
            logger.error(getClass().getSimpleName(), e.getMessage());
            throw PException.buildException(WSErrorCode.NO_GOODS_ITEM, e);
        }
    }

    protected void getSiteSellHis(Item item, Document doc) {
        String sitesell = doc.select("div.cdtl_bn_top.cdtl_hb > a.cdtl_btn_more > span.cdtl_bn_sale > em").text();
        if (!sitesell.isEmpty()) {
            if (scrapValidator != null)
                ((SsgScrapValidator) scrapValidator).validateContentForSaleAmt(sitesell);
            sitesell = sitesell.replace(",", "");
            if (item instanceof ColtItem) {
                ((ColtItem) item).setColtItemSiteSellHis(((ColtItem) item).getItemNum(),
                        ((ColtItem) item).getCollectSite(), Integer.parseInt(sitesell));
            } else if (item instanceof ColtTghtSellItem) {
                ((ColtTghtSellItem) item).setColtItemSiteSellHis(((ColtTghtSellItem) item).getBundleItemNum(),
                        ((ColtTghtSellItem) item).getCollectSite(), Integer.parseInt(sitesell));
            }
        }

    }

    /**
     * 
     * <pre>
     *  Description : 번들 아이템에 각 아이템의 itemNum을 추출하여 새로 request 한다.
     * </pre>
     * 
     * @Method Name : bundleItemNum
     * @date : 2018. 2. 19.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 2. 19. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param doc
     * @return List<String> : 반환값을 아이템아이디로 request 하기위해 사용.
     */
    private List<String> bundleItemNum(Document doc) {
        List<String> bundle = new ArrayList<>();
        Elements scriptEL = doc.select("script");

        for (Element script : scriptEL) {
            Pattern pattern = Pattern.compile(PATTERN_03, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(script.html());
            String text = "";

            if (matcher.find()) {

                String[] spl = script.html().split(PATTERN_03_1);

                for (String sp : spl) {
                    Pattern pattern2 = Pattern.compile(PATTERN_03, Pattern.DOTALL);
                    Matcher matcher2 = pattern2.matcher(sp);

                    if (matcher2.find()) {

                        text = matcher2.group().trim();
                        text = text.replace("itemObj = {", "").replace("};", "").replace(", ", ",").replace("'", "")
                                .replaceAll("\\n", "").replaceAll("\\t", "").trim();

                        Map<String, String> map;
                        map = compileMap(text); // jsonObject 형식이 아니기 때문에 map
                                                // 형태로 변환하여 값을 저장시킨다.
                        bundle.add(plusurl +
                                map.get("itemId") +
                                "&siteNo=" + map.get("siteNo") +
                                "&salestrNo=" + map.get("salestrNo"));
                    }
                }
            }
        }
        return bundle;
    }

    protected Item parseItem(Long jobId, URI uri, String collectSite, String content) {
        String url = uri.toString();
        Document doc = Jsoup.parse(content);
        if (errorPage(doc, url))
            return null;
        // 다른브랜드가 어떠어떠한것들이 있는지 체크먼저 하는 코드. (ex-burberry , gucci 등)
        // 공식 브랜드가 있어서 디테일이 다를 경우가 있다.
        boolean bundleYn = isBundleItem(uri.toString());
        if (bundleYn) {
            return parseBundleSellingItem(jobId, uri, collectSite, content, doc);
        }

        try {
            String title = getTitle(doc);

            String itemId = doc.select("#itemId").attr("value");

            String siteNo = doc.select("#siteNo").attr("value");

            String salestrNo = doc.select("#salestrNo").attr("value");

            String itemNum = SsgCV.getItemNum(itemId, siteNo, salestrNo);

            String category = getCategoryInfo(doc);
            boolean detailcate = false;
            if (category != null && !category.equals(this.category))
                detailcate = true;
            ColtItem item = ColtItem.make(collectSite, title, category, itemNum, WSCode.COLT_ITEM_TYPE_FASHION,
                    WSCode.USE_WAY_ANAL, detailcate, collectDataInterval);
            String goodsNum = getGoodsNum(doc, title);
            if (!StringUtils.isNullOrEmpty(goodsNum)) // 상품자체코드가 없을 경우 itemNum으로 맞춘다.
                item.setOrgGoodsNum(goodsNum);

            item.setBrandName(getBrandName(doc, title)); // 2nd Parameter is
                                                         // bundle flag
            setdefault(item, content, uri, siteName);
            // 2020-10-06 trustSeller 수정로직
            setTrustSeller(item, doc, url);

            // 품절여부. 사이트에 품절이 떠도 재고량이 99999 일 경우가 있다. 그러므로 품절여부를 파악해야함.
            String soldout = doc.select("div#oriCart > ul.cdtl_btn_tbl").text();
            boolean soldoutYn = false;
            if (soldout.contains("품절") || soldout.contains("매진")) {
                soldoutYn = true;
                if (logger.isDebugEnabled()) {
                    logger.debug(getClass().getSimpleName(), String.format("품절 -> %s", uri));
                }
            }

            // 재고에 있는 사이즈,컬러 옵션 이외의 삼품들.
            option(item, doc);

            // 가격
            price(item, doc);

            // 제품 상세정보 (제조사, 제조년월 ,제조국 등..)
            productdetail(item, doc);
            // 평점 , 토탈리뷰 ,기타 정보(태그 등..)
            getAddtionalInfo(doc, item, itemId, siteNo, salestrNo);

            // 이미지
            images(item, doc, dataReductionHandler);
            // 재고
            if (!soldoutYn)
                getStockAmount(jobId, doc, item, dataReductionHandler);
            else {
                getStockAmountInOutofStock(item, doc, dataReductionHandler); // 재고값을 0으로 만들어야함
            }

            getSiteSellHis(item, doc);

            addDeliveryTag(doc, item);
            return item;
        } catch (Exception e) {
            logger.error(getClass().getSimpleName(), e.getMessage());
            throw PException.buildException(WSErrorCode.NO_GOODS_ITEM, e);
        }
    }

    protected void setTrustSeller(ColtItem item, Document document, String url) {
        Elements productEL = document
                .select("div.cdtl_sec >div.cdtl_cont_info > div.cdtl_tbl.ty2 > table > tbody > tr");
        String store = document.select("span.cdtl_store_tit").text();
        if (StringUtils.isNotNullOrEmpty(store)) {
            if (store.contains("스토어")) {
                String trustSeller = store.replaceAll("스토어", "");
                item.setTrustSeller(trustSeller);
            }
        } else {
            parseSeller(productEL, document, item);
        }

    }

    private void parseSeller(Elements productEL, Document document, ColtItem item) {
        if (!productEL.isEmpty()) {
            for (Element product : productEL) {
                String key = product.select("th > div").text();
                String value = product.select("td > div").text();
                if (value.length() < 200)
                    setSeller(key, value, item);
            }
        } else { // 기존 제품상세정보 이외의 파싱
            productEL = document
                    .select("div.info_lst > ul > li.on > div > div.viewport > div.cdtl_tbl.ty2 >table > tbody > tr");
            if (!productEL.isEmpty())
                for (Element product : productEL) {
                    String key = product.select("th > div").text();
                    String value = product.select("td > div").text();
                    if (value.length() < 200)
                        setSeller(key, value, item);
                }
        }

    }

    private String getQueryString(Elements select) {
        Map<String, String> map = null;
        for (Element el : select) {
            if (el.html().contains("var resultItemObj")) {
                String var = el.html().replaceAll("[\\s\r\t\n]", "");
                String param = SsgCV.getValue("varresultItemObj=[{](.*?)[\\/][\\/]", var).replace("'", "");
                map = paramInsertMap(param);
                String dispCtgLclsId = SsgCV.getValue("var_dispCtgLclsId=['](.*?)[']", var);
                String dispCtgMclsId = SsgCV.getValue("var_dispCtgMclsId=['](.*?)[']", var);
                String dispCtgSclsId = SsgCV.getValue("var_dispCtgSclsId=['](.*?)[']", var);
                map.put("dispCtgLclsId", StringUtils.isNotNullOrEmpty(dispCtgLclsId) ? dispCtgLclsId : "");
                map.put("dispCtgMclsId", StringUtils.isNotNullOrEmpty(dispCtgMclsId) ? dispCtgMclsId : "");
                map.put("dispCtgSclsId", StringUtils.isNotNullOrEmpty(dispCtgSclsId) ? dispCtgSclsId : "");
            }
        }

        StringBuilder sb = new StringBuilder();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(String.format("%s=%s", entry.getKey().toString(), entry.getValue().toString()));
            }
        }
        return sb.toString();
    }

    private Map<String, String> paramInsertMap(String param) {
        Map<String, String> map = new HashMap<>();
        String[] arr = param.split(",");
        String[] keysArr = { "sellprc", "brandId", "brandNm", "stdCtgId", "itemRegDivCd", "itemRegDivCd",
                "itemSellTypeCd", "itemSellTypeDtlCd", "itemSellTypeDtlCd", "etcBrandYn", "dispSiteNo",
                "dispCtgId", "grpAddrId", "shppcstId", "splVenId", "lrnkSplVenId" };
        for (String keyValue : arr) {
            String[] arr2;
            arr2 = keyValue.split(":", 2);
            if (isCollect(keysArr, keyValue)) {
                String key = arr2[0];
                String value = arr2[1];
                map.put(key, value);
            }
        }
        return map;
    }

    private boolean isCollect(String[] array, String keyValue) {
        return Arrays.stream(array).collect(Collectors.toList()).stream().filter(i -> keyValue.contains(i)).count() > 0
                ? true
                : false;
    }

    protected boolean errorPage(Document doc, String url) {

        String html = doc.select("script").html();
        if (html.contains("판매가 종료된 상품입니다.")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(), String.format("판매가 종료된 상품입니다. -> %s", url));
                dataReductionHandler.updateEndDtInColtBsUrl(baseUrlItem.getId());
            }
            boolean bundleYn = isBundleItem(url);
            if (bundleYn == false) {
                dataReductionHandler.insertSellEndItem(baseUrlItem.getId(),
                        SsgCV.getItemNum(SsgCV.getItemId(url), SsgCV.getSiteNo(url), SsgCV.getSalestrNo(url)));
            }
            return true;
        } else if (html.contains("올바르지 않은 접근 경로입니다.")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(), String.format(" 올바르지 않은 접근 경로입니다. -> %s", url));
            }
            return true;
        } else if (html.contains("행사 기간이 아닙니다.")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(), String.format(" 행사 기간이 아닌 상품입니다.. -> %s", url));
            }
            return true;
        } else if (doc.select("title").text().contains("19금 성인인증")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(), String.format(" 성인인증이 필요한 페이지입니다. -> %s", url));
            }
            dataReductionHandler.updateEndDtInColtBsUrl(baseUrlItem.getId()); // 핀메종료
            return true;
        } else if (html.contains("임직원 전용 상품 입니다.")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(), String.format(" 임직원 전용 상품입니다. -> %s", url));
            }
            dataReductionHandler.updateEndDtInColtBsUrl(baseUrlItem.getId()); // 핀메종료
            return true;
        } else if (doc.select("title").text().contains("시스템 점검")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getSimpleName(),
                        String.format(" 사이트 점검 중 입니다. -> %s , 점검일정 -> %s", url, doc.select("div.ssgerr_bx").text()));
            }
            return true;

        }
        return false;
    }

    /**
     * 
     * <pre>
     *  Description : 공통작업이 필요할 경우.
     * </pre>
     * 
     * @Method Name : setdefault
     * @date : 2018. 3. 2.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 3. 2. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param item
     * @param collectedItemId
     * @param content
     * @param uri
     * @throws IOException
     */
    public void setdefault(ColtItem item, String content, URI uri, String siteName) throws IOException {
        item.setReleaseDt(DateUtils.getCurrentDate());
        item.setCollectDay(DateUtils.getCurrentDate());
        item.setSiteName(siteName);
        item.setCollectUrl(uri.toString());
        item.setOrgUrl("");

    }

    /**
     * 
     * <pre>
     *  Description : 오픈마켓은 컬러 , 사이즈 , 스타일.. 순서로 옵션을 저장한다.
     * </pre>
     * 
     * @Method Name : option
     * @date : 2018. 2. 19.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 2. 19. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param item
     * @param doc
     */
    protected void option(ColtItem item, Document doc) {
        int i = 0;
        Elements EL = doc.select("div#_ordOpt_area >dl.cdtl_dl.cdtl_opt_group");
        if (!EL.isEmpty())
            for (Element E : EL) {
                String name = E.select("dl.cdtl_dl.cdtl_opt_group > dt").text();
                Elements optionEL = E.select("dl.cdtl_dl.cdtl_opt_group> dd > select > option");
                if (i == 0) { // 컬러옵션
                    i++;
                    item.addLabelToColorOption(name);
                    for (Element opEL : optionEL) {
                        String option = opEL.attr("value");
                        if (option.isEmpty())
                            continue;
                        item.addColorOption(option);
                        if (scrapValidator != null)
                            ((SsgScrapValidator) scrapValidator).validateContentForColor(option);
                    }
                } else if (i == 1) { // 사이즈옵션
                    i++;
                    item.addLabelToSizeOption(name);
                    for (Element opEL : optionEL) {
                        String option = opEL.attr("value");
                        if (option.isEmpty())
                            continue;
                        item.addSizeOption(option);
                    }
                } else if (i == 2) {// 스타일옵션
                    i++;
                    item.addLabelToStyleOption(name);
                    for (Element opEL : optionEL) {
                        String option = opEL.attr("value");
                        if (option.isEmpty())
                            continue;
                        item.addStyleOption(option);
                    }
                } else {// 새로운 옵션
                    item.addLabelToOption(name);
                    for (Element opEL : optionEL) {
                        String option = opEL.attr("value");
                        if (option.isEmpty())
                            continue;
                        item.addOption(option);
                    }
                }
            }
        else {
            // 옵션 없이 단일상품.
        }
    }

    /**
     * 
     * <pre>
     *  Description : 품절상품. stock 을 0으로 만든다.
     * </pre>
     * 
     * @Method Name : getStockAmountInOutofStock
     * @date : 2018. 2. 13.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 2. 13. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param item
     * @param doc
     */
    protected void getStockAmountInOutofStock(ColtItem item, Document doc, DataReduction dataReduction) {
        Long id = item.getId();
        if (id != null && id != -1) {
            List<ColtItemIvt> lastStockList = dataReduction.getLastStockInfoByItem(item);

            for (ColtItemIvt ivt : lastStockList) {
                ivt.setStockAmount(0);
                if (scrapValidator != null)
                    ((SsgScrapValidator) scrapValidator).increaseStockCount();
            }

            item.setColtItemIvt(lastStockList);
        } else {
            Elements scriptEL = doc.select("script");

            for (Element script : scriptEL) {
                Pattern pattern = Pattern.compile(PATTERN_01, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(script.html());
                String text = "";

                if (matcher.find()) {

                    String[] spl = script.html().split(PATTERN_01_1);

                    for (String sp : spl) { // 포문 1번당 하나의 스큐.
                        Pattern pattern2 = Pattern.compile(PATTERN_01, Pattern.DOTALL);
                        Matcher matcher2 = pattern2.matcher(sp);

                        if (matcher2.find()) {
                            String color = "";
                            String size = "";
                            String style = "";
                            String gift = "";
                            String option = "";
                            int qty = 0;

                            text = matcher2.group().trim();
                            text = text.replace("uitemObj = {", "").replace("};", "").replace(", ", ",")
                                    .replace("'", "").replaceAll("\\n", "").replaceAll("\\t", "").trim();

                            Map<String, String> map;
                            map = compileMap(text); // jsonObject 형식이 아니기 때문에
                                                    // map 형태로 변환하여 값을 저장시킨다.

                            String uitemId = map.get("uitemId");

                            if (uitemId.equals("00000") && spl.length != 2)
                                continue; // spl 길이가 2인 경우는 단일 상품이기때문에 00000이
                                          // 상품이다.

                            for (int i = 1; i <= 5; i++) {
                                String name = map.get("uitemOptnTypeNm" + i);
                                if (name.isEmpty())
                                    break;
                                if (i == 1) { // 컬러옵션
                                    color = map.get("uitemOptnNm" + i);
                                    option = color;
                                } else if (i == 2) { // 사이즈옵션
                                    size = map.get("uitemOptnNm" + i);
                                    option += "/" + size;

                                } else if (i == 3) {// 스타일옵션
                                    style = map.get("uitemOptnNm" + i);
                                    option += "/" + style;

                                } else {// 기프트옵션 ssg에서 표현하는 값은 5가지까지 표현되지만
                                        // database에는 4가지 뿐이므로 4,5번째 값은 기프트로
                                        // 넣어준다.
                                    gift = map.get("uitemOptnNm" + i);
                                    option += "/" + gift;
                                }
                            }
                            /**
                             * 2018-11-16 KSE
                             * sku 단위로 최적가 표시!
                             */
                            String bestAmt = map.get("bestAmt");
                            bestAmt = StringUtils.removeForPrice(bestAmt);
                            String stockId = makeStockId(item.getItemNum(), option);

                            String ivtOption = "";
                            if (promotion) {
                                ivtOption = "Z";
                            }

                            item.addColtItemIvt(color, size, DateUtils.getCurrentDate(), ivtOption, style, gift,
                                    Integer.parseInt(bestAmt), qty, stockId);
                            if (scrapValidator != null)
                                ((SsgScrapValidator) scrapValidator).increaseStockCount();

                        }
                    }
                }
            }
        }
    }

    protected void getStockAmount(Long jobId, Document doc, ColtItem item, DataReduction dataReduction) {

        Elements scriptEL = doc.select("script");

        for (Element script : scriptEL) {
            Pattern pattern = Pattern.compile(PATTERN_01, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(script.html());

            Map<String, Integer> integrationStock = new HashMap<>();
            boolean integrationCh = false;
            String text = "";

            if (matcher.find()) {
                String[] spl2 = script.html().split(PATTERN_02_1);
                for (String sp : spl2) {
                    Pattern pattern2 = Pattern.compile(PATTERN_02, Pattern.DOTALL);
                    Matcher matcher2 = pattern2.matcher(sp);
                    if (matcher2.find()) {
                        integrationCh = true;
                        /*
                         * 새로운 가능성 일 경우. ( ex- 만약 처음 보여준 페이지에 재고량이 없는 경우. )
                         * String color=""; String size=""; String style="";
                         * String gift=""; String option="";
                         */
                        Integer qty = 0;
                        String uitemId = "";

                        text = matcher2.group().trim();
                        text = text.replace("salestrUitemList = {", "").replace("};", "").replace(", ", ",")
                                .replace("'", "").replaceAll("\\n", "").replaceAll("\\t", "").trim();

                        Map<String, String> map;
                        map = compileMap(text);

                        uitemId = map.get("uitemId");
                        qty = Integer.parseInt(map.get("usablInvQty"));

                        if (integrationStock.get(uitemId) != null) {
                            qty += integrationStock.get(uitemId);
                            integrationStock.put(uitemId, qty);
                        } else {
                            integrationStock.put(uitemId, qty);
                        }
                    }
                }

                String[] spl = script.html().split(PATTERN_01_1);

                for (String sp : spl) { // 포문 1번당 하나의 스큐.
                    Pattern pattern2 = Pattern.compile(PATTERN_01, Pattern.DOTALL);
                    Matcher matcher2 = pattern2.matcher(sp);

                    if (matcher2.find()) {
                        String color = "";
                        String size = "";
                        String style = "";
                        String gift = "";
                        String option = "";
                        Integer qty = 0;
                        String uitemId = "";

                        text = matcher2.group().trim();
                        text = text.replace("uitemObj = {", "").replace("};", "").replace(", ", ",").replace("'", "")
                                .replaceAll("\\n", "").replaceAll("\\t", "").trim();
                        if (scrapValidator != null)
                            ((SsgScrapValidator) scrapValidator).validateContentForStock(text);

                        Map<String, String> map;
                        map = compileMap(text); // jsonObject 형식이 아니기 때문에 map
                                                // 형태로 변환하여 값을 저장시킨다.

                        uitemId = map.get("uitemId"); // uitemId 가 00000인건 통합
                                                      // 재고임으로 사용하지 않는다.
                        if (uitemId.equals("00000") && spl.length != 2)
                            continue; // spl 길이가 2인 경우는 단일 상품이기때문에 00000이 상품이다.

                        qty = Integer.parseInt(map.get("usablInvQty"));

                        for (int i = 1; i <= 5; i++) {
                            String name = map.get("uitemOptnTypeNm" + i);
                            if (name.isEmpty())
                                break;
                            if (i == 1) { // 컬러옵션
                                color = map.get("uitemOptnNm" + i);
                                option = color;
                            } else if (i == 2) { // 사이즈옵션
                                size = map.get("uitemOptnNm" + i);
                                option += "/" + size;

                            } else if (i == 3) {// 스타일옵션
                                style = map.get("uitemOptnNm" + i);
                                option += "/" + style;

                            } else {// 기프트옵션 ssg에서 표현하는 값은 5가지까지 표현되지만
                                    // database에는 4가지 뿐이므로 4,5번째 값은 기프트로 넣어준다.
                                gift = map.get("uitemOptnNm" + i);
                                option += "/" + gift;
                            }
                        }

                        String stockId = makeStockId(item.getItemNum(), option);

                        if (integrationCh && integrationStock.get(uitemId) != null)
                            qty += integrationStock.get(uitemId);

                        /**
                         * 2018-11-16 KSE
                         * sku 단위로 최적가 표시!
                         */
                        String bestAmt = map.get("bestAmt");
                        bestAmt = StringUtils.removeForPrice(bestAmt);

                        if (map.get("invMngYn") != null && map.get("invMngYn").equals("N"))
                            qty = WSCode.STOCK_UNKNOWN;

                        String ivtOption = "";
                        if (promotion) {
                            ivtOption = "Z";
                        }

                        item.addColtItemIvt(color, size, DateUtils.getCurrentDate(), ivtOption, style, gift,
                                Integer.parseInt(bestAmt), qty, stockId);
                        if (scrapValidator != null)
                            ((SsgScrapValidator) scrapValidator).increaseStockCount();

                    }
                }
            }
        }

    }

    protected void getAddtionalInfo(Document doc, ColtItem item, String itemId, String siteNo, String salestrNo) {
        String totalEvt = doc.select("em#postngNlistCnt").text();
        if (totalEvt.equals("0") || totalEvt.isEmpty()) {
            item.setTotalEvalCnt(0);
            item.setFivePoint(0D);
        } else {
            totalEvt = totalEvt.replace(",", "");
            Elements fivePointEL = doc.select("div.cdtl_grade_area > span.cdtl_grade_num > em.cdtl_grade_total");
            String fivePoint = "0.0";
            if (!fivePointEL.isEmpty()) {
                fivePoint = doc.select("div.cdtl_grade_area > span.cdtl_grade_num > em.cdtl_grade_total").text();
            } else if (!doc.select("p.cdtl_review_txt > span.num").isEmpty()) {
                fivePoint = doc.select("p.cdtl_review_txt > span.num").text();
            } else if (!doc.select("span.cdtl_star_score > span.cdtl_txt").isEmpty()) { // 샤넬
                fivePoint = doc.select("span.cdtl_star_score > span.cdtl_txt").text();
            } else {
                if (logger.isDebugEnabled())
                    logger.debug(getClass().getSimpleName(), String.format("5점만점 크롤링 예외 -> %s", item.getCollectUrl()));
            }

            Integer totalEvalut = Integer.parseInt(totalEvt);
            item.setTotalEvalCnt(totalEvalut);
            item.setFivePoint(Double.parseDouble(fivePoint));
            if (isEvalut) {
                Integer page = totalEvalut <= 0 ? 1 : 2;
                IntStream.rangeClosed(1, page)
                        .forEach(x -> {
                            Get get = Get.make(String.format(SsgCV.COMMENT_LIST_URL, itemId, siteNo, x));
                            Header header = SsgCV.makeEvalutHeader(item.getCollectUrl());
                            List<CommentItem> itemList = requestCommentList(getJobId(), getParam(), header, get, this);
                            item.addColtItemEvalut(itemList);
                        });
            }

        }

        for (Element e : doc.select("div.cdtl_prd_info > span > i > span")) {
            item.addTag(e.text());
        }

        /** 20200429 KSE 배송방법 수집 */
        String deiv = doc.select("div.deiv_bdg.bdg_b > span > span").text();
        if (StringUtils.isNotNullOrEmpty(deiv))
            item.addTag(deiv);/** 20200429 KSE 배송방법 수집 */

    }

    @Override
    public List<CommentItem> parseComment(URI url, String collectSite, String content) throws PException {
        JSONParser parser = new JSONParser();
        JSONObject jsonOb = new JSONObject();

        try {
            jsonOb = (JSONObject) parser.parse(content);
        } catch (ParseException e) {
            logger.error(getClass().getSimpleName(),
                    String.format("parseComment Error, url -> %s, error: %s", url, e.getMessage()));
        }
        List<CommentItem> commentOrigin = getProductEvaluation(url, jsonOb);
        int i = 0;
        // 중복 제거 ColtList
        Map<String, ColtItemEvalut> map = new HashMap<>();
        for (CommentItem commentItem : commentOrigin) {
            ColtItemEvalut evalut = (ColtItemEvalut) commentItem;
            String goodsComment = evalut.getGoodsComment();
            goodsComment = goodsComment.replaceAll("\\s", "").replaceAll("\\n", "");
            String user = evalut.getRegId();
            if (map.containsKey(user)) {
                ColtItemEvalut evalOrigin = (ColtItemEvalut) map.get(user);
                String evalutOrigin = evalOrigin.getGoodsComment();
                evalutOrigin = evalutOrigin.replaceAll("\\s", "").replaceAll("\\n", "");
                // 사용자가 같은데 내용다른경우만
                if (!goodsComment.equals(evalutOrigin)) {
                    user = user + i;
                    map.put(user, evalOrigin);
                }

            } else {
                map.put(user, evalut);
            }
            i++;
        }
        List<CommentItem> commentList = new ArrayList<>();
        for (String key : map.keySet()) {
            ColtItemEvalut evalut = map.get(key);
            commentList.add(evalut);
        }

        return commentList;
    }

    private List<CommentItem> getProductEvaluation(URI url, JSONObject jsonOb) {
        List<CommentItem> commentList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        jsonObject = (JSONObject) jsonOb.get("pageDto");

        JSONArray jsonArr = (JSONArray) jsonObject.get("resultList");
        for (Object reviewJson : jsonArr) {

            JSONObject review = (JSONObject) reviewJson;
            String mbrLoginId = review.get("mbrLoginId") + "";
            String comment = review.get("postngCntt") + "";
            String date = review.get("wrtDt") + "";
            String star = review.get("recomEvalScr") + "";

            if (StringUtils.isNullOrEmpty(comment))
                comment = WSCode.REVIEW_EMPTY;

            ColtItemEvalut evalut = ColtItemEvalut.make(date, comment, star, mbrLoginId);
            evalut.setRegId(mbrLoginId);
            evalut.setNewEvalut();

            commentList.add(evalut);

        }
        return commentList;
    }

    protected void images(ColtItem item, Document doc, DataReduction dataReduction) {
        String img = "";
        Long id = item.getId();
        String color = StringUtils.defaultIfEmpty(item.getColorOption(), "");
        Elements imageEL = doc.select(
                "div.cdtl_pager_sec > div.cdtl_pager > div.cdtl_pager_slide >ul.cdtl_pager_lst >li > ul.lst_thmb > li");
        if (!imageEL.isEmpty()) {
            if (scrapValidator != null)
                ((SsgScrapValidator) scrapValidator).validateContentForImage(imageEL);
            for (Element image : imageEL) {
                img = URLUtils.addSchema("http:", image.select("img").attr("src"));
                img = makeBigImage(img, 500);
                if (id != null && id != -1) {
                    item.addColtImage(id, color, img, dataReduction);

                } else {
                    item.addColtImage(color, img);
                }
            }
        } else {
            img = doc.select("img#mainImg").attr("src");
            if (id != null && id != -1) {
                item.addColtImage(id, color, img, dataReduction);
            } else {
                item.addColtImage(color, img);
            }
        }
    }

    /**
     * 이미지를 큰 이미지로 변경한다. ssg에서는 이미지 이름 마지막에 이미지 크기를 지정하도록 되어 있다.
     * http://item.ssgcdn.com/33/59/36/item/1000025365933_i1_60.jpg 위 이미지는 60x60
     * 이미지이며 마지막의 60을 500으로 변경하면 500x500 이미지가 된다.
     * 
     * @param src
     *             이미지명
     * @param size
     *             변경하고자 하는 크기
     * @return 변경된 이미지명
     */
    protected static String makeBigImage(String src, int size) {
        if (src == null || src.trim().length() == 0) {
            return null;
        }
        if (src.contains("img.ssgcdn.com")) {
            return src.replace("w=70&h=70", "w=500&h=500");
        }

        int pos = src.lastIndexOf("_");
        StringBuilder sb = new StringBuilder();
        if (pos != -1) {
            int dotPos = src.lastIndexOf(".");
            sb.append(src.substring(0, pos + 1));
            sb.append(size);
            sb.append(src.substring(dotPos));

            return sb.toString();
        }
        return src;
    }

    protected void productdetail(ColtItem item, Document doc) {
        Elements productEL = doc.select("div.cdtl_sec >div.cdtl_cont_info > div.cdtl_tbl.ty2 > table > tbody > tr");
        if (!productEL.isEmpty())
            for (Element product : productEL) {
                String key = product.select("th > div").text();
                String value = product.select("td > div").text();

                if (value.length() < 200) {
                    /**
                     * 데이터가 들어가는 것을 보면서 차근차근 수정.
                     */
                    item.addJsonData(key, value);
                    setMaterial(key, value, item);
                    setMadeCountry(key, value, item);
                    setMadeDt(key, value, item);
                    setIngredients(key, value, item);
                    setVolume(key, value, item);
                }

            }
        else { // 기존 제품상세정보 이외의 파싱 (버버리)
            productEL = doc
                    .select("div.info_lst > ul > li.on > div > div.viewport > div.cdtl_tbl.ty2 >table > tbody > tr");
            if (!productEL.isEmpty())
                for (Element product : productEL) {
                    String key = product.select("th > div").text();
                    String value = product.select("td > div").text();
                    if (value.length() < 200) {
                        /**
                         * 데이터가 들어가는 것을 보면서 차근차근 수정.
                         */
                        item.addJsonData(key, value);
                        setMaterial(key, value, item);
                        setMadeCountry(key, value, item);
                        setMadeDt(key, value, item);
                        setIngredients(key, value, item);
                        setVolume(key, value, item);
                    }

                }
        }

        String deal = doc.select("span.cdtl_new_price > span").text();
        if (StringUtils.isNotNullOrEmpty(deal))
            if (deal.contains("원~"))
                item.addJsonData("type", "deal");

        // 2021-03-29 배송사추가
        Elements deliverList = doc.select("div.cdtl_item >dl.cdtl_dl > dd> ul >li > div");
        if (deliverList.hasClass("ssg-tooltip-wrap cdtl_ly_wrap cdtl_ly_delivcmpny")) {
            for (Element element : deliverList) {
                String deliCompany = element.select("a.cdtl_btn_delivery").text().trim();
                if (StringUtils.isNotNullOrEmpty(deliCompany)) {
                    item.addJsonData("배송사", deliCompany);

                }
            }
        }

        // 2021-03-29 배송비 무료 여부 추가
        Elements deliverFeeEl = doc.select("div.cdtl_lst > div > dl.cdtl_dl.cdtl_delivery_fee >dd > ul > li");
        StringBuffer deliverFee = new StringBuffer();
        int i = 0;
        for (Element element : deliverFeeEl) {
            element.select("> div").remove();
            String fee = element.text();
            deliverFee.append(fee);
            if (deliverFeeEl.size() != 1 && i == 0)
                deliverFee.append(" / ");
            i++;
        }
        item.addJsonData("배송비", deliverFee.toString());

        item.setAddInfo(item.getJsonDataToString());
    }

    protected static void setIngredients(String key, String body, ColtItem cItem) {
        String[] keyIndicators = { "성분" };
        boolean contain = StringUtils.contain(keyIndicators, key.replaceAll(" ", ""));

        if (contain) {
            cItem.setIngredients(body);
        }
    }

    protected static void setVolume(String key, String body, ColtItem cItem) {
        String[] keyIndicators = { "용량", "중량" };
        boolean contain = StringUtils.contain(keyIndicators, key.replaceAll(" ", ""));

        if (contain) {
            cItem.setVolume(body);
        }
    }

    protected static void setSeller(String key, String body, ColtItem cItem) {
        String[] keyIndicators = { "제조업자", "제조사", "수입자", "제조판매업자" };
        boolean contain = StringUtils.contain(keyIndicators, key.replaceAll(" ", ""));

        if (contain) {
            cItem.setTrustSeller(body);
        }
    }

    protected static void setMaterial(String key, String body, ColtItem cItem) {
        String[] keyIndicators = { "소재" };
        boolean contain = StringUtils.contain(keyIndicators, key.replaceAll(" ", ""));

        if (contain && !key.contains("소재지"))// 소재로 파싱할때 소재지는 제외. 주소가 들어갈 수 있다.
        {
            cItem.setMaterials(body);
        }
    }

    protected static void setMadeCountry(String key, String body, ColtItem cItem) {
        String[] keyIndicators = { "제조국" };
        boolean contain = StringUtils.contain(keyIndicators, key.replaceAll(" ", ""));

        if (contain) {
            cItem.setMaftOrigin(body);
        }
    }

    protected static void setMadeDt(String key, String body, ColtItem cItem) {
        String[] keyIndicators = { "제조연월", "제조년월", "출시" };
        boolean contain = StringUtils.contain(keyIndicators, key.replaceAll(" ", ""));

        if (contain) {
            cItem.setMaftDt(body);
            cItem.setOrgMaftDt(body);
        }
    }

    /**
     * 
     * <pre>
     *  Description : jsoup을 이용하여 가격 파싱.
     * </pre>
     * 
     * @Method Name : price
     * @date : 2018. 2. 12.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 2. 12. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param item
     * @param doc
     */
    protected void price(ColtItem item, Document doc) {
        /**
         * 2018-09-19 KSE
         * 추가개발
         */
        String price = doc.select("#sellprc").attr("value");
        String divPrice = doc.select("#bestAmt").attr("value");
        if (scrapValidator != null)
            ((SsgScrapValidator) scrapValidator).validateContentForDiscountRate(doc.select("#sellprc"));
        if (price.contains(divPrice)) {
            item.setPrice(price);
            item.setSitePrice(price);

        } else {
            item.setPrice(price);
            item.setSitePrice(price);
            item.addColtItemDiscount(divPrice, StringUtils.calculateRate2(price, divPrice));
        }
    }

    protected String getTitle(Document doc) {
        String title = "";

        Elements el = doc.select("#itemNm");
        title = el.attr("value").trim();
        if (scrapValidator != null)
            ((SsgScrapValidator) scrapValidator).validateContentForGoodsName(title);
        return title;
    }

    protected String getBundleTitle(Document doc) {
        String title = "";
        Elements el = doc.select(
                "div.cdtl_cm_detail.ty_hotdeal.react-area > div.cdtl_row_top > div.cdtl_col_rgt > div.cdtl_prd_info > h2");
        title = el.text().trim();
        if (scrapValidator != null)
            ((SsgScrapValidator) scrapValidator).validateContentForGoodsName(title);
        return title;
    }

    protected String getBrandName(Document doc, String title) {
        String brand = "";

        Elements brandEL = doc.select("div.cdtl_prd_info > span > a");

        // 2023-02-06 브랜드네임 파싱 변경
        if (brandEL.isEmpty()) {
            brandEL = doc.select("div.cdtl_prd_info > h2 > span > a");
        }

        if (brandEL.isEmpty()) {
            brandEL = doc.select("a.cdtl_info_tit_link");
        }
        if (brandEL.isEmpty()) {
            List<String> tList = fashionBrandRepo.getBrandString(title);
            if (tList != null) {
                if (tList.size() == 2) {
                    for (String tt : tList) {
                        if (fashionBrandRepo.isBrandName(tt)) {
                            return tt;
                        }
                    }

                    for (String tt : tList) {
                        if (fashionBrandRepo.isSite(tt)) {
                            tList.remove(tt);
                            return tList.get(0);
                        }
                    }

                } else {
                    for (String tt : tList) {
                        if (fashionBrandRepo.isBrandName(tt)) {
                            return tt;
                        }
                    }
                }
                return ""; // no brand name
            }
        }

        brand = brandEL.text().replaceAll("#", "");
        return brand;
    }

    protected String getGoodsNum(Document doc, String title) {
        String goodsNum = null;

        String modelNum = doc.select("#item_detail_contents > p.cdtl_model_num").text();
        if (!modelNum.isEmpty()) {
            goodsNum = modelNum.replace("모델번호 : ", "");
        }
        if (goodsNum == null || goodsNum.trim().length() == 0) {
            String tempGoodsNum = GoodNumUtils.extractGoodNum(title);
            if (tempGoodsNum != null) {
                goodsNum = tempGoodsNum;
            }
        }
        if (goodsNum != null) { // 값이 75보다 클 경우 DB에 goodsnum 허용량이 (100) 이므로 처리
                                // 한글도 있을 수 있기 때문에 75정도로한다.
            if (goodsNum.length() > 75)
                return null;
        }
        return goodsNum;
    }

    protected boolean isBundleItem(String uri) {
        return uri.contains("dealItemView.ssg?");
    }

    protected String getCategoryInfo(Document doc) {
        String mall = "";
        String category = "";

        mall = doc.select("span.cmall_ic > span > span.blind").text();

        Elements categoryEL = doc.select("#location > div.lo_depth_01");
        Elements categoryEL2 = doc.select("div.cate_location > div.inner > div.depth");
        Elements categoryEL3 = doc
                .select("div.location_top > div.special_location > div.pr_top > div.pr_path > ul.pr_pathlist >li");
        Elements categoryEL4 = doc.select("div#content > div.location_path > ul.location_pathlist > li");

        if (!categoryEL.isEmpty()) {
            for (Element categoryELE : categoryEL) {// 기본 페이지 상세카테고리 파싱
                category += categoryELE.child(0).text();
                if (!categoryELE.select("span.lo_line").isEmpty())
                    category += categoryELE.select("span.lo_line").text();
            }
        } else if (!categoryEL2.isEmpty()) { // MAC , tomford
            category = "SSG.COM";
            if (siteName.equals(SsgCV.DEPARTMENT_SITE_NAME)) {
                category = SsgCV.DEPARTMENT_SITE_NAME;
            }
            for (Element categoryELE : categoryEL2) {
                category += categoryELE.select("div.depth > a").text();
                if (!categoryELE.select("div.depth > span.line").isEmpty()) {
                    category += ">";
                }
            }

            if (!siteName.equals(SsgCV.DEPARTMENT_SITE_NAME)) {
                category = category.replaceAll("SSG.COM", "SSG.COM>신세계백화점");
            }

        } else if (!categoryEL3.isEmpty()) { // burberry
            category = "SSG.COM";
            if (siteName.equals(SsgCV.DEPARTMENT_SITE_NAME)) {
                category = SsgCV.DEPARTMENT_SITE_NAME;
            }
            for (Element categoryELE : categoryEL3) {
                category += ">" + categoryELE.select("a > span").text();
            }

            if (!siteName.equals(SsgCV.DEPARTMENT_SITE_NAME)) {
                category = category.replaceAll("SSG.COM", "SSG.COM>신세계몰");
            }
        } else if (!categoryEL4.isEmpty()) { // boots
            category = "SSG.COM";
            if (siteName.equals(SsgCV.DEPARTMENT_SITE_NAME)) {
                category = SsgCV.DEPARTMENT_SITE_NAME;
            }
            for (Element categoryELE : categoryEL4) {
                category += categoryELE.select("span.arr").text();
                category += categoryELE.select("span.tx").text();
            }
            category = category.replace("Home", "Boots");
        }

        if (category == null || category.trim().length() == 0) {
            return this.category;
        }

        if (!mall.isEmpty()) {
            category = category.replaceAll("SSG.COM", "SSG.COM>" + mall);
            if (siteName.equals(SsgCV.DEPARTMENT_SITE_NAME)) {
                category = category.replaceAll("DepartmentSsg", "DepartmentSsg > " + mall);
            }
        }

        return category;
    }

    /**
     * 
     * <pre>
     *  Description : 상세 페이지 안에 있는 재고량 부분을 map 형식으로 바꾼다.
     * </pre>
     * 
     * @Method Name : compileMap
     * @date : 2018. 2. 19.
     * @author : 김상은
     * @history :
     *          -----------------------------------------------------------------------
     *          Date Writer Content ----------- -------------------
     *          --------------------------------------- 2018. 2. 19. 김상은 Initial
     *          Write
     *          -----------------------------------------------------------------------
     * 
     * @param text
     * @return
     */
    protected Map<String, String> compileMap(String text) {
        Map<String, String> map = new HashMap<>();
        String[] arr = text.split(",");
        for (String keyValue : arr) {
            String[] arr2;
            arr2 = keyValue.split(":", 2);
            if (arr2.length == 2) {
                map.put(arr2[0], arr2[1]);
            } else {
                map.put(arr2[0], "");
            }
        }

        return map;
    }

    protected static String makeStockId(String itemNum, String option) {
        return "" + StringUtils.toHash(itemNum + option);
    }

    @Override
    public Item call() throws Exception {
        Item item = null;
        try {
            try {
                String url = getGet().getUrl();
                String itemNum = SsgCV.getItemNum(SsgCV.getItemId(url), SsgCV.getSiteNo(url), SsgCV.getSalestrNo(url));
                if (!StringUtils.isNullOrEmpty(itemNum)) {
                    boolean collectedItemInTime = dataReductionHandler.isCollectedItemInTime(itemNum, COLL_SITE,
                            TimeUnit.MINUTES, collectDataInterval);

                    if (collectedItemInTime) {
                        dataReductionHandler.updateProcDtInColtBsUrl(baseUrlItem.getId());
                        if (logger.isDebugEnabled()) {
                            logger.debug(getClass().getSimpleName(), "RUN",
                                    "Already Scraped Item ->{},BaseUrlId ->{} In Today", itemNum, baseUrlItem.getId());
                        }
                        return null;
                    }

                    item = request(getJobId(), getParam(), getHeader(), getGet(), this);
                    return item;
                } else {
                    item = request(getJobId(), getParam(), getHeader(), getGet(), this);
                    return item;
                }
            } catch (PException p) {
                if (p.getErrorNumber() == WSErrorCode.NOT_READY_PARSE_HTML) {
                    logger.error(getClass().getSimpleName(), "NOT_READY_PARSE_HTML error >> " + getGet().getUrl());
                    throw PException.buildException(WSErrorCode.TOO_MANY_NOT_READY_PARSE_HTML, p.getCause());
                } else {
                    if (p.getErrorNumber() == WSErrorCode.NO_GOODS_BRAND_NAME) {
                        dataReductionHandler.disableProcDtInColtBsUrl(baseUrlItem.getId());
                        baseUrlItem.setStatus(WSCode.DEAD);
                    }
                    throw p;
                }
            }

        } catch (Exception e) {
            logger.error(getClass().getSimpleName(), e);
            logger.error(getClass().getSimpleName(), "error>>" + getGet().getUrl());
            ColtRetryItem retryItem = ColtRetryItem.make(getExtract(), getGet().getUrl(), getGet().getTestUrl(), this,
                    getHeader().getValue("Host"), getHeader().getValue("Origin"), getHeader().getValue("Referer"),
                    COLL_SITE, getJobId());
            return retryItem;
        } finally {
            updateBaseUrlId(item, baseUrlItem);
        }
    }

    private String getItemNum(String url) {
        String[] patterns = { "itemId=(\\w+)" };
        for (String pattern : patterns) {
            List<String> keys = MatchUtils.parseGroup(pattern, url);
            if (keys != null && !keys.isEmpty()) {
                return keys.get(0);
            }
        }
        return null;
    }

    protected void addDeliveryTag(Document doc, ColtItem item) {
        Elements deliveryInfo = doc.select("div.cdtl_item >dl.cdtl_dl > dd> ul >li > span.cdtl_ico_delivery");
        for (Element el : deliveryInfo) {
            String delInfo = el.text();
            if (StringUtils.isNotNullOrEmpty(delInfo) && delInfo.contains("해외")) {
                item.addTag(WSCode.FOREIGN_DEL);
            }
        }
    }

    @Override
    public void updatePostParam(Map<String, String> param, Post post) {
    }

    @Override
    public void updateBaseUrlId(Item item, ColtBaseUrlItem baseUrlItem) {
        Long baseUrlId;
        if (item != null && baseUrlItem != null) {
            baseUrlId = baseUrlItem.getId();
            int eventYn = baseUrlItem.getEventYn();
            if (item instanceof ColtItem) {
                ((ColtItem) item).setBaseUrlId(baseUrlId);
                ((ColtItem) item).setEventYn(eventYn);
                ((ColtItem) item).setReleaseDt(baseUrlItem.getReleaseDate());
            } else if (item instanceof ColtTghtSellItem) {
                ((ColtTghtSellItem) item).setBaseUrlId(baseUrlId);

                /** 190902 KSE 번들 아이템 relbsurl 모든 아이템 추가 **/
                for (ColtItem tt : ((ColtTghtSellItem) item).getBundleItems())
                    tt.setBaseUrlId(baseUrlId);

                ((ColtTghtSellItem) item).setEventYn(eventYn);
                ((ColtTghtSellItem) item).setReleaseDt(baseUrlItem.getReleaseDate());
            } else if (item instanceof ColtRetryItem) {
                ((ColtRetryItem) item).setBaseUrlId(baseUrlId);
                ((ColtRetryItem) item).setEventYn(eventYn);
                ((ColtRetryItem) item).setReleaseDt(baseUrlItem.getReleaseDate());
            }
        }
    }

    @Override
    public boolean checkAccessDenined(int reponseCode, String content, String url, AtomicInteger sleepPeriod,
            AtomicBoolean cacheCleanYn) {

        if (content.equals("Proxy Error")) {
            logger.debug(getClass().getSimpleName(), "proxy error !!! to retry responseCode = " + reponseCode);
            return true;
        }
        ;

        return false;
    }

    @Override
    public boolean checkAccessRestricted(int reponseCode, String content, String url, AtomicInteger sleepPeriod,
            AtomicBoolean cacheCleanYn) {
        if (reponseCode == 401) {
            sleepPeriod.set(1000);
            cacheCleanYn.set(true);
            return true;
        }
        return false;
    }

    @Override
    public String toShortString() {

        return null;
    }
}
