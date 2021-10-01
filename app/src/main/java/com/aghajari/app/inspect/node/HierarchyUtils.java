package com.aghajari.app.inspect.node;

import android.content.res.Resources;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.aghajari.app.inspect.pages.hierarchy.TreeNode;

public final class HierarchyUtils {

    public static String getDebugViewHierarchy(@NonNull AccessibilityNodeInfo v) {
        StringBuilder desc = new StringBuilder();
        getViewHierarchy(v, desc, 0);
        return desc.toString();
    }

    private static void getViewHierarchy(AccessibilityNodeInfo v, StringBuilder desc, int margin) {
        if (v == null) return;
        desc.append(getViewMessage(v, margin));
        if (v.getChildCount() > 0) {
            margin++;
            for (int i = 0; i < v.getChildCount(); i++) {
                getViewHierarchy(v.getChild(i), desc, margin);
            }
        }
    }

    private static String getViewMessage(AccessibilityNodeInfo v, int marginOffset) {
        String repeated = new String(new char[marginOffset]).replace("\0", "  ");
        try {
            return repeated + "[" + v.getClassName() + "] " + v.getViewIdResourceName() + "\n";
        } catch (Resources.NotFoundException e) {
            return repeated + "[" + v.getClassName() + "] name_not_found\n";
        }
    }

    public static TreeNode<NodeInfo> getViewHierarchy(@NonNull AccessibilityNodeInfo v) {
        TreeNode<NodeInfo> root = new TreeNode<>(new NodeInfo(0, v, 0));
        getViewHierarchy(v, root, new TagInstance<>(0));
        return root;
    }

    private static void getViewHierarchy(AccessibilityNodeInfo v, TreeNode<NodeInfo> node, TagInstance<Integer> position) {
        if (v == null) return;
        node.expand();

        if (v.getChildCount() > 0) {
            int max = v.getChildCount();
            for (int i = 0; i < max; i++) {
                position.tag++;

                AccessibilityNodeInfo v2 = v.getChild(i);
                NodeInfo info = new NodeInfo(position.tag, v2, max > 1 ? i + 1 : 0);
                TreeNode<NodeInfo> node2 = new TreeNode<>(info);
                node.addChild(node2);

                getViewHierarchy(v2, node2, position);
            }
        }
    }

    public static NodeInfo findNodeInfo(@NonNull AccessibilityNodeInfo target, AccessibilityNodeInfo root) {
        return findNodeInfo(target, root, new TagInstance<>(0), 0);
    }

    private static NodeInfo findNodeInfo(AccessibilityNodeInfo target, AccessibilityNodeInfo v, TagInstance<Integer> position, int childIndex) {
        if (v == null) return null;
        if (target.equals(v))
            return new NodeInfo(position.tag, v, childIndex);

        if (v.getChildCount() > 0) {
            int max = v.getChildCount();
            for (int i = 0; i < max; i++) {
                position.tag++;
                AccessibilityNodeInfo v2 = v.getChild(i);
                NodeInfo info = findNodeInfo(target, v2, position, max > 1 ? i + 1 : 0);
                if (info != null)
                    return info;
            }
        }
        return null;
    }

}
