package model;


import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ComboboxToolTipRenderer extends DefaultListCellRenderer {
    List<String> tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {

        JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);
    	ToolTipManager.sharedInstance().setInitialDelay(200);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
        if (-1 < index && null != value && null != tooltips) {
            list.setToolTipText(tooltips.get(index));
        }
        return comp;
    }

    public void setTooltips(List<String> tooltips) {
        this.tooltips = tooltips;
    }
}