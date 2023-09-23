package tomasulo;

import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Table extends JPanel {

    JTable table;

    public Table(String[][]data, String[]header, int[]bounds) {

        DefaultTableModel model = new DefaultTableModel(data, header);

        table = new JTable(model);

        table.setPreferredScrollableViewportSize(new Dimension(bounds[2], bounds[3]));
        table.setFillsViewportHeight(true);
        table.setBounds(bounds[0],bounds[1],bounds[2],bounds[3]);
        table.setRowHeight(30);
//        table.setRowMargin(10);

//        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);




        JScrollPane js = new JScrollPane(table);
        js.setVisible(true);
        add(js);

    }
    public TableModel getModel() {
        return table.getModel();
    }
}