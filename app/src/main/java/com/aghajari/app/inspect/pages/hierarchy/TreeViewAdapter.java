package com.aghajari.app.inspect.pages.hierarchy;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.inspect.R;
import com.aghajari.app.inspect.node.LayoutSettings;
import com.aghajari.app.inspect.node.NodeInfo;
import com.aghajari.app.inspect.utils.Utils;
import com.aghajari.app.inspect.views.InspectView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tlh on 2016/10/1 :)
 * <p>
 * SOURCE : https://github.com/TellH/RecyclerTreeView
 */
class TreeViewAdapter<T> extends RecyclerView.Adapter<TreeViewAdapter.ViewHolder> implements View.OnClickListener {
    private static final String KEY_IS_EXPAND = "IS_EXPAND";
    final List<TreeNode> displayNodes;
    private int padding;
    private OnTreeNodeListener onTreeNodeListener;
    private boolean toCollapseChild;

    public TreeViewAdapter(TreeNode node) {
        displayNodes = new ArrayList<>();
        padding = Utils.dp(8);

        if (node != null)
            findDisplayNodes(Arrays.asList(node));
    }

    public void setRoot(TreeNode node) {
        displayNodes.clear();
        if (node != null)
            findDisplayNodes(Arrays.asList(node));
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout node_layout;
        final TextView node_text;
        final TextView node_text2;
        final ImageView arrow;
        final View selector;
        final View arrow_click_area;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            node_layout = itemView.findViewById(R.id.node_layout);
            node_text = itemView.findViewById(R.id.node_text);
            node_text2 = itemView.findViewById(R.id.node_text2);
            arrow = itemView.findViewById(R.id.arrow);
            selector = itemView.findViewById(R.id.selector_color);
            arrow_click_area = itemView.findViewById(R.id.arrow_click_area);
        }

        public void bind(final TreeViewAdapter adapter, final TreeNode tree) {
            final NodeInfo info = (NodeInfo) tree.getContent();
            final InspectView.AccessibilityNodeInfoData data = LayoutSettings.getInstance().data;
            boolean selected = data != null && info.isSelected(data.getFinalInfo());
            info.findTexts(selected);

            arrow.setVisibility(tree.hasChild() ? View.VISIBLE : View.GONE);
            arrow.setRotation(tree.isExpand() ? 0 : -90);
            arrow_click_area.setEnabled(tree.hasChild());
            arrow_click_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.changeState(ViewHolder.this);
                }
            });

            if (selected) {
                selector.setBackgroundColor(Color.TRANSPARENT);
                itemView.setBackgroundColor(info.bgColor);
            } else if (data != null && info.isParentOfNode(data.getFinalInfo())) {
                selector.setBackgroundColor(info.selectorColor);
                itemView.setBackgroundColor(info.bgSelectorColor);
            } else {
                selector.setBackgroundColor(info.selectorColor);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            if (selected) {
                arrow.setColorFilter(Color.WHITE);
                node_text.setTextColor(Color.WHITE);
                node_text2.setTextColor(Color.WHITE);
            } else {
                node_text.setTextColor(Color.BLACK);
                node_text2.setTextColor(Color.BLACK);
                arrow.setColorFilter(null);
            }

            node_text2.setEllipsize(selected ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
            node_text2.setSelected(selected);
            node_text.setText(info.text);
            node_text2.setText(info.text2);

            itemView.setTag(this);
            itemView.setOnClickListener(adapter);
        }

        private ObjectAnimator objectAnimator;

        void arrowAnimation(boolean expand, long duration) {
            if (objectAnimator != null)
                objectAnimator.cancel();

            objectAnimator = ObjectAnimator.ofFloat(arrow, "rotation", 0, -90);
            objectAnimator.setDuration(duration);
            if (expand && arrow.getRotation() == 0)
                objectAnimator.start();
            else if (!expand && arrow.getRotation() == -90)
                objectAnimator.reverse();
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getLayoutPosition();
            LayoutSettings.getInstance().select((NodeInfo) (displayNodes.get(position).getContent()));
        }
    }

    /**
     * 从nodes的结点中寻找展开了的非叶结点，添加到displayNodes中。
     *
     * @param nodes 基准点
     */
    private void findDisplayNodes(List<TreeNode> nodes) {
        for (TreeNode<?> node : nodes) {
            displayNodes.add(node);
            if (!node.isLeaf() && node.isExpand())
                findDisplayNodes(node.getChildList());
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tree_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((FrameLayout.LayoutParams) holder.node_layout.getLayoutParams()).setMarginStart(
                Utils.dp(40) + displayNodes.get(position).getHeight() * padding);

        holder.bind(this, displayNodes.get(position));
    }

    private void changeState(final ViewHolder holder) {
        TreeNode<?> selectedNode = displayNodes.get(holder.getLayoutPosition());
        // Prevent multi-click during the short interval.
        try {
            long lastClickTime = (long) holder.arrow.getTag();
            if (System.currentTimeMillis() - lastClickTime < 500)
                return;
        } catch (Exception e) {
            holder.arrow.setTag(System.currentTimeMillis());
        }
        holder.arrow.setTag(System.currentTimeMillis());

        if (onTreeNodeListener != null && onTreeNodeListener.onClick(selectedNode, holder))
            return;
        if (selectedNode.isLeaf())
            return;
        // This TreeNode<?> was locked to click.
        if (selectedNode.isLocked()) return;


        boolean isExpand = selectedNode.isExpand();
        int positionStart = displayNodes.indexOf(selectedNode) + 1;
        holder.arrowAnimation(isExpand, 200);

        if (!isExpand) {
            notifyItemRangeInserted(positionStart, addChildNodes(selectedNode, positionStart));
        } else {
            notifyItemRangeRemoved(positionStart, removeChildNodes(selectedNode, true));
        }
    }

    private int addChildNodes(TreeNode pNode, int startIndex) {
        List<TreeNode> childList = pNode.getChildList();
        int addChildCount = 0;
        for (TreeNode TreeNode : childList) {
            displayNodes.add(startIndex + addChildCount++, TreeNode);
            if (TreeNode.isExpand()) {
                addChildCount += addChildNodes(TreeNode, startIndex + addChildCount);
            }
        }
        if (!pNode.isExpand())
            pNode.toggle();
        return addChildCount;
    }

    private int removeChildNodes(TreeNode pNode) {
        return removeChildNodes(pNode, true);
    }

    private int removeChildNodes(TreeNode pNode, boolean shouldToggle) {
        if (pNode.isLeaf())
            return 0;
        List<TreeNode<?>> childList = pNode.getChildList();
        int removeChildCount = childList.size();
        displayNodes.removeAll(childList);
        for (TreeNode<?> child : childList) {
            if (child.isExpand()) {
                if (toCollapseChild)
                    child.toggle();
                removeChildCount += removeChildNodes(child, false);
            }
        }
        if (shouldToggle)
            pNode.toggle();
        return removeChildCount;
    }

    @Override
    public int getItemCount() {
        return displayNodes == null ? 0 : displayNodes.size();
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void ifCollapseChildWhileCollapseParent(boolean toCollapseChild) {
        this.toCollapseChild = toCollapseChild;
    }

    public void setOnTreeNodeListener(OnTreeNodeListener onTreeNodeListener) {
        this.onTreeNodeListener = onTreeNodeListener;
    }

    public interface OnTreeNodeListener {
        /**
         * called when TreeNodes were clicked.
         *
         * @return weather consume the click event.
         */
        boolean onClick(TreeNode<?> node, RecyclerView.ViewHolder holder);

        /**
         * called when TreeNodes were toggle.
         *
         * @param isExpand the status of TreeNode<?>s after being toggled.
         */
        void onToggle(boolean isExpand, RecyclerView.ViewHolder holder);
    }

    public void refresh(List<TreeNode> TreeNodes) {
        displayNodes.clear();
        findDisplayNodes(TreeNodes);
        notifyDataSetChanged();
    }

    public Iterator<TreeNode> getDisplayNodesIterator() {
        return displayNodes.iterator();
    }

    private void notifyDiff(final List<TreeNode> temp) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return temp.size();
            }

            @Override
            public int getNewListSize() {
                return displayNodes.size();
            }

            // judge if the same items
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return TreeViewAdapter.this.areItemsTheSame(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
            }

            // if they are the same items, whether the contents has bean changed.
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return TreeViewAdapter.this.areContentsTheSame(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
            }

            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                return TreeViewAdapter.this.getChangePayload(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
            }
        });
        diffResult.dispatchUpdatesTo(this);
    }

    private Object getChangePayload(TreeNode oldNode, TreeNode newNode) {
        Bundle diffBundle = new Bundle();
        if (newNode.isExpand() != oldNode.isExpand()) {
            diffBundle.putBoolean(KEY_IS_EXPAND, newNode.isExpand());
        }
        if (diffBundle.size() == 0)
            return null;
        return diffBundle;
    }

    // For DiffUtil, if they are the same items, whether the contents has bean changed.
    private boolean areContentsTheSame(TreeNode<?> oldNode, TreeNode<?> newNode) {
        return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent())
                && oldNode.isExpand() == newNode.isExpand();
    }

    // judge if the same item for DiffUtil
    private boolean areItemsTheSame(TreeNode<?> oldNode, TreeNode<?> newNode) {
        return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent());
    }

    /**
     * collapse all root nodes.
     */
    public void collapseAll() {
        // Back up the nodes are displaying.
        List<TreeNode> temp = backupDisplayNodes();
        //find all root nodes.
        List<TreeNode> roots = new ArrayList<>();
        for (TreeNode displayNode : displayNodes) {
            if (displayNode.isRoot())
                roots.add(displayNode);
        }
        //Close all root nodes.
        for (TreeNode root : roots) {
            if (root.isExpand())
                removeChildNodes(root);
        }
        notifyDiff(temp);
    }

    @NonNull
    private List<TreeNode> backupDisplayNodes() {
        List<TreeNode> temp = new ArrayList<>();
        for (TreeNode displayNode : displayNodes) {
            temp.add(displayNode.clone());
        }
        return temp;
    }

    public void collapseNode(TreeNode pNode) {
        List<TreeNode> temp = backupDisplayNodes();
        removeChildNodes(pNode);
        notifyDiff(temp);
    }

    public void collapseBrotherNode(TreeNode pNode) {
        List<TreeNode> temp = backupDisplayNodes();
        if (pNode.isRoot()) {
            List<TreeNode> roots = new ArrayList<>();
            for (TreeNode displayNode : displayNodes) {
                if (displayNode.isRoot())
                    roots.add(displayNode);
            }
            //Close all root nodes.
            for (TreeNode root : roots) {
                if (root.isExpand() && !root.equals(pNode))
                    removeChildNodes(root);
            }
        } else {
            TreeNode<?> parent = pNode.getParent();
            if (parent == null)
                return;
            List<TreeNode> childList = parent.getChildList();
            for (TreeNode node : childList) {
                if (node.equals(pNode) || !node.isExpand())
                    continue;
                removeChildNodes(node);
            }
        }
        notifyDiff(temp);
    }

}