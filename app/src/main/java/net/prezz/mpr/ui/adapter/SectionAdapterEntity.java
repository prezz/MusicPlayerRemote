package net.prezz.mpr.ui.adapter;

public class SectionAdapterEntity implements AdapterEntity {

    private static final long serialVersionUID = -5553831187058499677L;

    private String section;


    public SectionAdapterEntity(String section) {
        this.section = section;
    }

    @Override
    public String getSectionIndexText() {
        return getText();
    }

    @Override
    public String getText() {
        return section;
    }

    @Override
    public String toString() {
        return section;
    }
}
