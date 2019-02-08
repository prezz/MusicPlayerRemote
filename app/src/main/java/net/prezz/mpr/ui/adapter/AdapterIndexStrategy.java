package net.prezz.mpr.ui.adapter;

import java.util.ArrayList;

public interface AdapterIndexStrategy {

	void createSectionIndexes(AdapterEntity[] inEntities, ArrayList<String> outSectionsList, ArrayList<Integer> outPositionForSection, ArrayList<Integer> outSectionForPosition);
}
