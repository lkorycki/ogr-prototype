package ogr.model;

import java.util.Vector;

import javax.swing.AbstractListModel;

import ogr.util.DirectoryReader;

public class ItemListModel extends AbstractListModel<Item> {

	Vector<Item> data;
	
	public ItemListModel(Vector<Item> data) {
		this.data = data;
	}
	
	@Override
	public Item getElementAt(int index) {
		return data.get(index);
	}
	
	public void removeElementAt(int index) {
		data.get(index).delete();
		data.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public int getSize() {
		return data.size();
	}
	
	public void updateAll() {
		replaceData();
		super.fireContentsChanged(this, 0, getSize());
	}
	
	private void replaceData() {
		DirectoryReader dr = new DirectoryReader("D:/Photos");
		data = dr.getItems();
	}

}
