package ogr;

import java.io.File;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.opencv.core.Rect;

import ogr.engine.ImageProcessor.FilterColor;
import ogr.model.Item;
import ogr.model.ItemListModel;
import ogr.model.ItemListSelectionHandler;
import ogr.util.DirectoryReader;
import ogr.view.MainPanelView;
import ogr.view.MainWindowView;

public class MApplication {
	
	private JList<Item> itemList; // Item's list
	private int currentSelectionIndex; // Index of selected item in itemList
	private MainWindowView mainWindow; // main window
	private MainPanelView mainPanel; // with image and graph
	private ItemListModel itemListModel;
	private FilterColor textColor; // for filtering
	
	public MApplication() {
		initializeItemList();
		textColor = FilterColor.RED; // default
	}
	
	public FilterColor getColor()	{
		return textColor;
	}
	
	public void setColor(FilterColor _textColor)	{
		textColor = _textColor;
	}
	
	public void setMainPanel(MainPanelView _mainPanel)	{
		mainPanel = _mainPanel;
	}
	
	public MainPanelView getMainPanel() {
		return mainPanel;
	}	
	
	public ItemListModel getItemListModel() {
		return itemListModel;
	}

	public JList<Item> getItemList() {
		return itemList;
	}
	
	public Item getCurrentItem() {
		if(currentSelectionIndex == -1) return null;
		else return itemList.getModel().getElementAt(currentSelectionIndex);
	}
	
	public int getCurrentItemIdx() {
		return currentSelectionIndex;
	}
	
	public void setCurrentItem(int idx) {
		this.currentSelectionIndex = idx;
		mainPanel.getImagePanel().setImage(getCurrentItem().getImage());
		mainPanel.getGraphPanel().getController().setGraph(getCurrentItem().getGraph());
	}
	
	public File getCurrentImage() {
		return itemList.getModel().getElementAt(currentSelectionIndex).getImage();
	}
	
	private void initializeItemList() {
		DirectoryReader dr = new DirectoryReader("src/test/resources/test_images");
		itemListModel = new ItemListModel(dr.getItems());
		
		currentSelectionIndex = -1;
		itemList = new JList<Item>(itemListModel);
		ListSelectionModel lsm = itemList.getSelectionModel();
		lsm.addListSelectionListener(new ItemListSelectionHandler(this));
	}
	
	public void setMainWindow(MainWindowView ref) {
		mainWindow = ref;
	}
	
	public MainWindowView getMainWindow() {
		return mainWindow;
	}
	
	public Rect getSelection() {
		return mainPanel.getImagePanel().getSelection();
	}
}
