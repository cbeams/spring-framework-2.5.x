/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Map;

/**
 * @author Keith Donald
 */
public interface SubFlowAttributesMapper {
    public Map createSpawnedSubFlowAttributesMap(AttributesAccessor parentFlowAttributes);

    public void mapToResumingParentFlow(AttributesAccessor subFlowAttributes,
            MutableAttributesAccessor parentFlowAttributes);
}