package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.simplefitness.R;

import java.util.List;

import model.Workout;


/**
 * RecycleView 原理。
 *
 * 1. 核心组件
 * a. RecyclerView
 * RecyclerView是展示数据集合的组件，它提供了滚动功能和高效的视图回收机制。它比ListView更加灵活和强大，支持水平和垂直滚动，以及网格和瀑布流布局。
 *
 * b. LayoutManager
 * LayoutManager负责RecyclerView中项目的布局，它定义了如何测量和放置子视图。RecyclerView提供了以下几种LayoutManager：
 *
 * LinearLayoutManager：线性布局，支持垂直和水平滚动。
 * GridLayoutManager：网格布局，支持垂直和水平滚动。
 * StaggeredGridLayoutManager：交错网格布局，支持瀑布流效果。
 * 开发者可以根据需要选择适合的布局管理器，或者自定义布局管理器。
 *
 * c. Adapter
 * Adapter负责提供数据，并为RecyclerView中的每个项目创建视图。它继承自RecyclerView.Adapter，并需要实现以下方法：
 *
 * onCreateViewHolder(ViewGroup parent, int viewType)：创建新的ViewHolder。
 * onBindViewHolder(VH holder, int position)：将数据绑定到ViewHolder上。
 * getItemCount()：返回数据集中的项目总数。
 *
 * Adapter持有所有数据，并创建每个视图、并给视图绑定数据。  是RecycleView数据 和 视图 的适配器。 这里视图是由ViewHolder来处理。
 *
 *
 * d. ViewHolder
 * ViewHolder用于缓存视图以提高滚动性能。它继承自RecyclerView.ViewHolder，并包含对项目视图的引用，以便快速访问。
 *
 *
 * 使用Adapter可以
 * 多样化的数据展示：
 * 在现实应用中，列表或网格中的每一项可能有不同的布局。例如，新闻应用中，新闻列表的第一条可能是头条新闻，带有大图和详细描述，而其他新闻可能只有标题和小图。自定义Adapter和ViewHolder可以灵活地处理这些不同的布局需求。
 * 数据处理差异：
 * 不同的数据项可能需要不同的处理逻辑。例如，某些项可能需要绑定点击事件，而其他项可能需要不同的交互逻辑。自定义Adapter可以根据数据项的不同执行特定的逻辑。
 * 解耦数据和视图：
 * 自定义Adapter可以将数据和视图逻辑解耦，使得数据处理和视图展示可以独立变化，降低代码的耦合度，提高代码的可维护性。
 *
 * 使用ViewHolder可以：
 * 性能优化：
 * 通过自定义ViewHolder，可以缓存视图的引用，避免在onBindViewHolder中重复调用findViewById，从而提高列表滚动的性能。
 *
 *
 *
 *
 * =========
 *
 * 确实，自定义`Adapter`和`ViewHolder`是`RecyclerView`中非常关键的概念，它们提供了`RecyclerView`所需的灵活性和强大的数据处理能力。下面详细解释自定义`Adapter`和`ViewHolder`的理由和原理：
 *
 * ### 理由
 *
 * 1. **多样化的数据展示**：
 *    - 在现实应用中，列表或网格中的每一项可能有不同的布局。例如，新闻应用中，新闻列表的第一条可能是头条新闻，带有大图和详细描述，而其他新闻可能只有标题和小图。自定义`Adapter`和`ViewHolder`可以灵活地处理这些不同的布局需求。
 *
 * 2. **数据处理差异**：
 *    - 不同的数据项可能需要不同的处理逻辑。例如，某些项可能需要绑定点击事件，而其他项可能需要不同的交互逻辑。自定义`Adapter`可以根据数据项的不同执行特定的逻辑。
 *
 * 3. **性能优化**：
 *    - 通过自定义`ViewHolder`，可以缓存视图的引用，避免在`onBindViewHolder`中重复调用`findViewById`，从而提高列表滚动的性能。
 *
 * 4. **解耦数据和视图**：
 *    - 自定义`Adapter`可以将数据和视图逻辑解耦，使得数据处理和视图展示可以独立变化，降低代码的耦合度，提高代码的可维护性。
 *
 * ### 原理
 *
 * 1. **Adapter的作用**：
 *    - `Adapter`继承自`RecyclerView.Adapter`，负责创建`ViewHolder`、绑定数据到`ViewHolder`以及提供数据集的大小。
 *    - `Adapter`中的`onCreateViewHolder`方法负责根据不同的`viewType`创建不同的`ViewHolder`实例。
 *    - `onBindViewHolder`方法负责将数据绑定到对应的`ViewHolder`上，这个方法会随着列表的滚动频繁调用，因此其性能直接影响到`RecyclerView`的滚动性能。
 *
 * 2. **ViewHolder的作用**：
 *    - `ViewHolder`继承自`RecyclerView.ViewHolder`，是一个泛型类，持有所有视图元素的引用。
 *    - 在`onCreateViewHolder`中，`ViewHolder`被创建并初始化视图引用。
 *    - 在`onBindViewHolder`中，`ViewHolder`中的视图被更新为新数据，这样可以避免重复的视图查找和创建操作，提高性能。
 *
 * 3. **视图绑定**：
 *    - `ViewHolder`中的视图引用在列表项复用时保持不变，只有数据会发生变化。这种方式类似于`ListView`中的`getView()`方法，但是通过`ViewHolder`模式，可以更高效地管理视图和数据。
 *
 * 4. **视图类型的管理**：
 *    - `RecyclerView`支持多种视图类型（`viewType`）共存于同一个`RecyclerView`中。`Adapter`可以根据数据项的类型返回不同的`viewType`，从而创建和绑定不同类型的`ViewHolder`。
 *
 * 通过自定义`Adapter`和`ViewHolder`，`RecyclerView`能够灵活地处理各种复杂的列表场景，同时保持高性能和良好的用户体验。这种设计模式是`RecyclerView`强大功能的核心，也是其在现代Android应用中广泛使用的原因之一。
 *
 *
 *
 *
 *
 * 在`RecyclerView`中，自定义`Adapter`和`ViewHolder`的机制通过以下方式解耦数据和视图：
 *
 * ### 1. 数据和视图的分离
 *
 * - **数据模型**：通常，你会有一个数据模型类，它包含了业务逻辑所需的数据字段，例如用户信息、文章内容等。
 * - **视图模型**：`ViewHolder`则负责定义视图的结构，包括哪些控件、如何布局等。
 * - **适配器（Adapter）**：`Adapter`作为数据模型和视图模型之间的桥梁，负责将数据模型中的数据传递给视图模型。
 *
 * ### 2. Adapter的作用
 *
 * - `Adapter`不直接处理视图的创建，而是定义了如何将数据与视图关联起来。它通过实现`onCreateViewHolder`和`onBindViewHolder`两个核心方法来实现数据和视图的解耦。
 * - 在`onCreateViewHolder`中，`Adapter`创建`ViewHolder`实例，但不涉及任何数据。
 * - 在`onBindViewHolder`中，`Adapter`将数据传递给`ViewHolder`，由`ViewHolder`负责将数据渲染到视图上。
 *
 * ### 3. ViewHolder的作用
 *
 * - `ViewHolder`持有视图的引用，它不关心数据从何而来，只负责根据传入的数据更新视图。
 * - `ViewHolder`的设计模式类似于MVC（Model-View-Controller）中的视图（View），它只处理视图相关的操作，而不涉及业务逻辑。
 *
 * ### 4. 数据绑定
 *
 * - 数据绑定是通过`Adapter`中的`onBindViewHolder`方法实现的，该方法接收两个参数：`ViewHolder`和位置（position）。`ViewHolder`负责渲染视图，而位置用于从数据集中获取当前项的数据。
 * - 这种方式允许`ViewHolder`独立于数据存在，它只需要知道如何根据传入的数据更新视图。
 *
 * ### 5. 视图类型的管理
 *
 * - `RecyclerView`支持多种视图类型（`viewType`），`Adapter`可以根据数据项的不同返回不同的`viewType`，从而创建和绑定不同类型的`ViewHolder`。
 * - 这进一步解耦了数据和视图，因为即使数据项的类型不同，`ViewHolder`也可以独立于数据类型存在，只需正确处理视图更新。
 *
 * ### 6. 动画和装饰
 *
 * - `RecyclerView`还提供了`ItemAnimator`和`ItemDecoration`等装饰器，它们进一步将视图的动画效果和装饰从数据和视图的逻辑中分离出来。
 *
 * 通过这种方式，`RecyclerView`的`Adapter`和`ViewHolder`机制实现了数据和视图的解耦，使得数据处理、视图展示和视图更新可以独立变化，提高了代码的可维护性和可扩展性。
 *
 *
 *
 *
 *
 *
 */


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    private List<Workout> workoutItems;
    // 添加一个构造器参数，用于点击事件的回调
    private OnItemClickListener onItemClickListener;
    // 定义接口
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // 添加一个构造器参数，用于删除事件的回调
    private OnItemDeleteListener onItemDeleteListener;

    // 定义接口
    public interface OnItemDeleteListener {
        void onDeleteItemClick(int position);
    }


    public WorkoutAdapter(List<Workout> workoutItems, OnItemClickListener listener, OnItemDeleteListener deleteListener) {
        this.workoutItems = workoutItems;
        this.onItemClickListener = listener;
        this.onItemDeleteListener = deleteListener;
    }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {

        System.out.println("onBindViewHolder...");
        // 使用 final 确保在内部类中可以正确引用
        final int currentPosition = holder.getAdapterPosition();
        Workout item = workoutItems.get(currentPosition);
        holder.tvTitle.setText(item.getType());
        String des = "";
        int resId = 0;
        switch (item.getType()){
            case "跑步":
                des = "提高心肺功能";
                resId =  R.drawable.ic_running;
                break;
            case "搏击":
                des = "增强力量";
                resId =  R.drawable.ic_boxing;
                break;
            case "瑜伽":
                des = "提高柔韧性";
                resId =  R.drawable.ic_yoga;
                break;
            case "舞蹈":
                des = "快乐健身";
                resId =  R.drawable.ic_dance;
                break;
        }
        holder.tvDescription.setText(des);
        holder.ivIcon.setImageResource(resId);

        holder.tvDuration.setText(String.valueOf(item.getDuration()));
        holder.tvDifficulty.setText(item.getDifficulty());

        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(v.getContext(), WorkoutDetailActivity.class);
//            intent.putExtra("workout_title", item.getTitle());
//            v.getContext().startActivity(intent);
            // 调用接口的 onItemClick 方法
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(currentPosition);
            }
        });

        holder.btnDel.setOnClickListener(v -> {
            // 调用接口的 onDeleteItemClick 方法
            if (onItemDeleteListener != null) {
                onItemDeleteListener.onDeleteItemClick(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workoutItems.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView ivIcon;
        TextView tvDuration, tvDifficulty;
        Button btnDel;

        WorkoutViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            btnDel = itemView.findViewById(R.id.btn_del);
        }
    }
}
