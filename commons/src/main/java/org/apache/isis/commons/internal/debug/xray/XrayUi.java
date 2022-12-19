/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.commons.internal.debug.xray;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.apache.isis.commons.collections.Can;

import lombok.RequiredArgsConstructor;
import lombok.val;

public class XrayUi extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private final XrayModel xrayModel;

    private static XrayUi INSTANCE;

    private static AtomicBoolean startRequested = new AtomicBoolean();
    private static CountDownLatch latch = null;

    public static void start(int defaultCloseOperation) {
        val alreadyRequested = startRequested.getAndSet(true);
        if(!alreadyRequested) {
            latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(()->new XrayUi(defaultCloseOperation));    
        }
    }

    public static void updateModel(Consumer<XrayModel> consumer) {
        if(startRequested.get()) {
            SwingUtilities.invokeLater(()->{
                consumer.accept(INSTANCE.xrayModel);
                ((DefaultTreeModel)INSTANCE.tree.getModel()).reload();
                _SwingUtil.setTreeExpandedState(INSTANCE.tree, true);
            });
        }
    }

    public static void waitForShutdown() {
        if(latch==null
                || INSTANCE == null) {
            return;
        }
        System.err.println("Waiting for XrayUi to shut down...");
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isXrayEnabled() {
        return startRequested.get();
    }
    
    protected XrayUi(int defaultCloseOperation) {

        //create the root node
        root = new DefaultMutableTreeNode("X-ray");
        
        xrayModel = new XrayModelSimple(root);

        //create the tree by passing in the root node
        tree = new JTree(root);

        tree.setShowsRootHandles(false);
        
        val detailPanel = layoutUIAndGetDetailPanel(tree);
        
        tree.getSelectionModel().addTreeSelectionListener((TreeSelectionEvent e) -> {
            
            val selPath = e.getNewLeadSelectionPath();
            if(selPath==null) {
                return; // ignore event
            }
            val selectedNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            val userObject = selectedNode.getUserObject();
            
            //detailPanel.removeAll();
            
            if(userObject instanceof XrayDataModel) {
                ((XrayDataModel) userObject).render(detailPanel);
            } else {
                val infoPanel = new JPanel();
                infoPanel.add(new JLabel("Details"));
                detailPanel.setViewportView(infoPanel);
            }
            
            detailPanel.revalidate();
            detailPanel.repaint();
            
            //System.out.println("selected: " + selectedNode.toString());
        });
        
        val popupMenu = new JPopupMenu();
        val deleteAction = popupMenu.add(new JMenuItem("Delete"));
        deleteAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedNodes();
            }
        });
        
        tree.setCellRenderer(new XrayTreeCellRenderer((DefaultTreeCellRenderer) tree.getCellRenderer()));
        
        tree.addMouseListener(new MouseListener() {
            
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
        tree.addKeyListener(new KeyListener() {

            @Override public void keyReleased(KeyEvent e) {}
            @Override public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeSelectedNodes();
                }
            }

        });
        
        this.setDefaultCloseOperation(defaultCloseOperation);
        this.setTitle("X-ray Viewer");
        this.pack();
        this.setSize(800, 600);
        
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        INSTANCE = this;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                latch.countDown();
            }
        });
    }
    
    private void removeSelectedNodes() {
        Can.ofArray(tree.getSelectionModel().getSelectionPaths())
        .forEach(path->{
            val nodeToBeRemoved = (MutableTreeNode)path.getLastPathComponent(); 
            if(nodeToBeRemoved.getParent()!=null) {
                ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(nodeToBeRemoved);
                xrayModel.remove(nodeToBeRemoved);
            }
        });
    }

    private JScrollPane layoutUIAndGetDetailPanel(JTree masterTree) {
        
        JScrollPane masterScrollPane = new JScrollPane(masterTree);
        JScrollPane detailScrollPane = new JScrollPane();
        
        //Create a split pane with the two scroll panes in it.
        val splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   masterScrollPane, detailScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(260);
 
        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        masterScrollPane.setMinimumSize(minimumSize);
        detailScrollPane.setMinimumSize(minimumSize);
        
        detailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(8);
 
        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(800, 600));
        
        getContentPane().add(splitPane);
        
        return detailScrollPane;
    }

    // -- CUSTOM TREE NODE ICONS
    
    @RequiredArgsConstructor
    class XrayTreeCellRenderer implements TreeCellRenderer {
        
        final DefaultTreeCellRenderer delegate; 

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value, 
                boolean selected, 
                boolean expanded, 
                boolean leaf, 
                int row, 
                boolean hasFocus) {
            
            val label = (DefaultTreeCellRenderer)
                    delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            Object o = ((DefaultMutableTreeNode) value).getUserObject();
            if (o instanceof XrayDataModel) {
                XrayDataModel dataModel = (XrayDataModel) o;
                URL imageUrl = getClass().getResource(dataModel.getIconResource());
                if (imageUrl != null) {
                    label.setIcon(new ImageIcon(imageUrl));
                }
                label.setText(dataModel.getLabel());
            }
            return label;
        }
    }
    

    
}
