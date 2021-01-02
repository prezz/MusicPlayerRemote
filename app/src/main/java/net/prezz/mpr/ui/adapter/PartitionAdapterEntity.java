package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.PartitionEntity;

public class PartitionAdapterEntity implements AdapterEntity {

    private PartitionEntity entity;


    public PartitionAdapterEntity(PartitionEntity partitionEntity) {
        this.entity = partitionEntity;
    }

    public PartitionEntity getEntity() {
        return entity;
    }

    @Override
    public String getSectionIndexText() {
        return getText();
    }

    @Override
    public String getText() {

        return entity.getPartitionName();
    }

    public String getSubText() {
        StringBuilder sb = new StringBuilder();

        AudioOutput[] outputs = entity.getPartitionOutputs();

        for (int i = 0; i < outputs.length; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(outputs[i].getOutputName());
        }

        return sb.toString();
    }
}
