package com.aghajari.app.inspect.pages.hierarchy;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.inspect.node.HierarchyUtils;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.node.NodeInfo;
import com.aghajari.app.inspect.utils.Utils;
import com.aghajari.app.inspect.views.InspectView;

public class HierarchyRecyclerView extends RecyclerView implements LayoutSettings.OnUpdatedListener {

    AccessibilityNodeInfo lastInfo = null;

    public HierarchyRecyclerView(@NonNull Context context,AccessibilityNodeInfo root) {
        super(context);

        setLayoutManager(new LinearLayoutManager(context, VERTICAL, false));
        setAdapter(new TreeViewAdapter(HierarchyUtils.getViewHierarchy(root)));
    }

    @Override
    public void onUpdate(@Nullable InspectView.AccessibilityNodeInfoData data) {
    }

    @Override
    public void onSelected(@Nullable InspectView.AccessibilityNodeInfoData data) {
        if (data == null) {
            getAdapter().notifyDataSetChanged();
            return;
        }

        TreeViewAdapter adapter = (TreeViewAdapter) getAdapter();
        checkSelectedNode(data.getFinalInfo(), (TreeNode) adapter.displayNodes.get(0));
        adapter.setRoot((TreeNode) adapter.displayNodes.get(0));

        TreeNode t = findSelectedNode(data.getFinalInfo(), (TreeNode) adapter.displayNodes.get(0));
        int index = adapter.displayNodes.indexOf(t);
        if (t != null && index >= 0) {
            data.color = ((NodeInfo) t.getContent()).bgSelectorColor;

            if (lastInfo != null && lastInfo.equals(data.getFinalInfo()))
                return;

            scrollToPosition(index);
        }
    }

    private void checkSelectedNode(AccessibilityNodeInfo selected, TreeNode tree) {
        if (((NodeInfo) tree.getContent()).isParentOfNode(selected))
            tree.expand();

        if (tree.hasChild()) {
            int count = tree.getChildList().size();
            for (int i = 0; i < count; i++)
                checkSelectedNode(selected, (TreeNode) tree.getChildList().get(i));
        }
    }

    private TreeNode findSelectedNode(AccessibilityNodeInfo selected, TreeNode tree) {
        if (((NodeInfo) tree.getContent()).isSelected(selected))
            return tree;

        if (tree.hasChild()) {
            int count = tree.getChildList().size();
            for (int i = 0; i < count; i++) {
                TreeNode t = findSelectedNode(selected, (TreeNode) tree.getChildList().get(i));
                if (t != null)
                    return t;
            }
        }

        return null;
    }

    @Override
    public void onContentUpdated(@Nullable AccessibilityNodeInfo root) {
        ((TreeViewAdapter) getAdapter()).setRoot(HierarchyUtils.getViewHierarchy(root));
    }

}
