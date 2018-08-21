/**
 *
 */
package de.hybris.wooliesegiftcard.widgets;

import java.util.Optional;

import java.util.List;
import com.google.common.collect.Lists;
import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionDataList;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;

/**
 * @author 653154
 *
 */
public class OrderPersonalizationMediaFilterController extends AbstractInitAdvancedSearchAdapter
{
	 protected static final String NAVIGATION_NODE_ID = "OrderPersonalisationMedia";
	 private static final String IMAGE_STATUS = "imageApprovalStatus";
	 private static final String HAS_ORDER = "hasOrder";
	 private static final String MEDIA_CODE = "PersonalisationMedia";
	@Override
	public void addSearchDataConditions(AdvancedSearchData searchData, Optional<NavigationNode> arg1) {
        final FieldType imageStatus = new FieldType();
        imageStatus.setDisabled(true);
        imageStatus.setSelected(true);
        imageStatus.setName(IMAGE_STATUS);
        final FieldType orderStatus = new FieldType();
        orderStatus.setDisabled(true);
        orderStatus.setSelected(true);
        orderStatus.setName(HAS_ORDER);
    	final List<SearchConditionData> searchConditions = Lists.newArrayList();
        searchConditions.add(new SearchConditionData(imageStatus, ImageApprovalStatus.PENDING, ValueComparisonOperator.EQUALS));
        searchConditions.add(new SearchConditionData(orderStatus, true, ValueComparisonOperator.EQUALS));
        final SearchConditionDataList innerConditionList = SearchConditionDataList.and(searchConditions);
        searchData.addConditionList(ValueComparisonOperator.AND, Lists.newArrayList(innerConditionList));
	}
	@Override
	public String getNavigationNodeId() {
		return NAVIGATION_NODE_ID;
	}
	@Override
	public String getTypeCode() {
		return MEDIA_CODE;
	}
}
