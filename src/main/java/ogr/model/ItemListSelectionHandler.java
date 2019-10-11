package ogr.model;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ogr.MApplication;


public class ItemListSelectionHandler implements ListSelectionListener {

	private MApplication mapp;
	
	public ItemListSelectionHandler(MApplication mapp) {
		this.mapp = mapp;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (lsm.isSelectionEmpty() || e.getValueIsAdjusting()) return;
		int idx = lsm.getMinSelectionIndex();
		int last_idx = lsm.getMaxSelectionIndex();
        if (idx == last_idx) {
        	mapp.setCurrentItem(idx);
        }
	}

}
