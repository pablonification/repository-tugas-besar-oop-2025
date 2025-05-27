package com.spakborhills.view;

import com.spakborhills.controller.GameController;
import com.spakborhills.model.Farm;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Util.PriceList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.DefaultListCellRenderer;

public class StoreDialog extends JDialog {

    private GameController gameController;
    private Farm farmModel;
    private PriceList priceList;
    private JList<Item> itemJList;
    private DefaultListModel<Item> listModel;
    private JSpinner quantitySpinner;
    private JLabel playerGoldLabel;

    public StoreDialog(Frame owner, GameController gameController, Farm farmModel) {
        super(owner, "Toko Spakbor Hills", true); // true for modal dialog
        this.gameController = gameController;
        this.farmModel = farmModel;
        this.priceList = farmModel.getPriceList();

        if (this.priceList == null) {
            JOptionPane.showMessageDialog(this, "Daftar harga tidak tersedia.", "Error Toko", JOptionPane.ERROR_MESSAGE);
            dispose(); // Close dialog if essential data is missing
            return;
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(owner); // Center on parent frame
        setLayout(new BorderLayout(10, 10)); // Add some padding

        initComponents();
        populateItemList();
        updatePlayerGoldDisplay();
    }

    private void initComponents() {
        // Top Panel: Player Gold
        playerGoldLabel = new JLabel("Gold: " + farmModel.getPlayer().getGold() + " G");
        playerGoldLabel.setFont(new Font("Arial", Font.BOLD, 18));
        playerGoldLabel.setBorder(new EmptyBorder(10, 10, 0, 10)); // Top, Left, Bottom, Right
        add(playerGoldLabel, BorderLayout.NORTH);

        // Center Panel: Item List
        listModel = new DefaultListModel<>();
        itemJList = new JList<>(listModel);
        itemJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemJList.setFont(new Font("Monospaced", Font.PLAIN, 16)); // Use Monospaced for alignment
        itemJList.setFixedCellHeight(25); // Make rows a bit taller
        itemJList.setCellRenderer(new ItemListCellRenderer()); // Set custom renderer
        JScrollPane scrollPane = new JScrollPane(itemJList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Item Tersedia"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel: Quantity, Buy, Cancel
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Quantity Panel
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel qtyLabel = new JLabel("Jumlah:");
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1)); // Min 1, Max 999
        quantitySpinner.setFont(new Font("Arial", Font.PLAIN, 16));
        ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField().setColumns(4); // Adjust width

        JButton minusButton = new JButton("-");
        minusButton.setFont(new Font("Arial", Font.BOLD, 16));
        minusButton.addActionListener(e -> {
            int currentValue = (int) quantitySpinner.getValue();
            if (currentValue > 1) {
                quantitySpinner.setValue(currentValue - 1);
            }
        });

        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("Arial", Font.BOLD, 16));
        plusButton.addActionListener(e -> {
            int currentValue = (int) quantitySpinner.getValue();
            quantitySpinner.setValue(currentValue + 1);
        });

        quantityPanel.add(qtyLabel);
        quantityPanel.add(minusButton);
        quantityPanel.add(quantitySpinner);
        quantityPanel.add(plusButton);
        controlPanel.add(quantityPanel, BorderLayout.WEST);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton buyButton = new JButton("Beli");
        buyButton.setFont(new Font("Arial", Font.BOLD, 16));
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBuy();
            }
        });

        JButton cancelButton = new JButton("Batal");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
        cancelButton.addActionListener(e -> dispose()); // Close dialog

        buttonPanel.add(buyButton);
        buttonPanel.add(cancelButton);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void populateItemList() {
        listModel.clear();
        List<Item> itemsForSale = gameController.getStoreItemsForDisplay();

        if (itemsForSale == null || itemsForSale.isEmpty()) {
            // No items to display, the list will remain empty.
            // The renderer will handle the empty list display if needed, or we can add a placeholder.
            // For now, an empty list is fine.
            return;
        }

        for (Item item : itemsForSale) {
            int buyPrice = priceList.getBuyPrice(item.getName());
            if (buyPrice >= 0 || item.getName().equals("Koran Edisi Baru")) {
                listModel.addElement(item); // Add Item object directly
            }
        }
        if (!listModel.isEmpty()) {
            itemJList.setSelectedIndex(0); // Select the first item
        }
    }

    private void updatePlayerGoldDisplay() {
        playerGoldLabel.setText("Gold: " + farmModel.getPlayer().getGold() + " G");
    }

    private void handleBuy() {
        Item selectedItem = itemJList.getSelectedValue();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Pilih item yang ingin dibeli.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String originalItemName = selectedItem.getName();

        int quantity = (int) quantitySpinner.getValue();

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari nol.", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = gameController.requestBuyItem(originalItemName, quantity);

        if (success) {
            JOptionPane.showMessageDialog(this, "Kamu berhasil membeli " + quantity + " " + originalItemName + "!", "Pembelian Berhasil", JOptionPane.INFORMATION_MESSAGE);
            updatePlayerGoldDisplay(); // Update gold display
            // No need to repaint GamePanel here, GameController should handle it after model update
        } else {
            JOptionPane.showMessageDialog(this, "Pembelian gagal. Pastikan kamu memiliki cukup Gold atau item tersedia.", "Pembelian Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom ListCellRenderer to display Item objects
    private class ItemListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Item) {
                Item item = (Item) value;
                int buyPrice = priceList.getBuyPrice(item.getName());
                // Calculate max name length for formatting (re-calculate or pass from outer class)
                // For simplicity, let's assume a fixed width or calculate on the fly for this cell
                // Calculate max name length for formatting (iterate over the model)
                int maxNameLength = 0;
                for (int i = 0; i < listModel.size(); i++) {
                    Object element = listModel.getElementAt(i);
                    if (element instanceof Item) {
                        maxNameLength = Math.max(maxNameLength, ((Item) element).getName().length());
                    }
                }
                maxNameLength = Math.max(maxNameLength, "Item Name".length()); // Ensure header fits

                setText(String.format("%-" + (maxNameLength + 2) + "s %d G", item.getName(), buyPrice));
            }
            // No else if (value instanceof String) needed, as we only add Item objects now.
            return this;
        }
    }
}
