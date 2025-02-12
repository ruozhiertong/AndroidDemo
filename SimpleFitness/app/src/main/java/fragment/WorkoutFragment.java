package fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simplefitness.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import activity.AddWorkoutActivity;
import adapter.WorkoutAdapter;
import database.DatabaseHelper;
import model.Workout;

public class WorkoutFragment extends Fragment {
    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private List<Workout> workoutItems;
    private DatabaseHelper dbHelper;

    private static final int ADD_REQUEST_CODE = 1; // 自定义请求码


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        System.out.println("onCreateView...");
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

//        dbHelper = new DatabaseHelper(getContext());
        dbHelper = DatabaseHelper.getInstance(this.getContext());

        workoutItems = dbHelper.getAllWorkoutItems();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        initWorkoutItems();
        adapter = new WorkoutAdapter(workoutItems, new WorkoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getContext(), AddWorkoutActivity.class);
                intent.putExtra("workout_item", workoutItems.get(position));
                intent.putExtra("position", position);
                startActivityForResult(intent, ADD_REQUEST_CODE);
            }
        }, new WorkoutAdapter.OnItemDeleteListener() {
            @Override
            public void onDeleteItemClick(int position) {

                System.out.println("delete: " +  position);
                // 删除数据库中的 Workout 记录
                Workout workoutToDelete = workoutItems.get(position);
                // 更新列表
                workoutItems.remove(position);
                dbHelper.deleteWorkout(workoutToDelete);
//                adapter.notifyDataSetChanged();
                adapter.notifyItemRemoved(position);
//                refreshWorkoutItems();
                // 重要：通知后续项目的位置变化。更新该位置之后的所有项的位置
                adapter.notifyItemRangeChanged(position, workoutItems.size() - position);

            }
        });
        recyclerView.setAdapter(adapter);


        // FloatingActionButton
        FloatingActionButton fabAddWorkout = view.findViewById(R.id.fab_add_workout);
        fabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理添加健身项目的逻辑
//                Intent intent = new Intent(getContext(), AddWorkoutActivity.class);
//                startActivity(intent);
                // 在 Fragment 中启动 Activity 时
                Intent intent = new Intent(getContext(), AddWorkoutActivity.class);
                startActivityForResult(intent, ADD_REQUEST_CODE);  // 使用 startActivityForResult()
            }
        });


        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult...");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            System.out.println("Activity.RESULT_OK");
            if (data != null){
                Workout returnData = data.getParcelableExtra("newWorkout");
                if (returnData != null){
                    System.out.println(returnData);
//            adapter.notifyDataSetChanged();
                    // 重新加载数据
//            refreshWorkoutItems();
                    // 添加到列表
                    workoutItems.add(returnData);
                    // 通知适配器新增了一项
                    adapter.notifyItemInserted(workoutItems.size() - 1);
                    // 可选：滚动到新添加的项目
                    recyclerView.scrollToPosition(workoutItems.size() - 1);
                }else{
                    Workout updateWorkout = data.getParcelableExtra("updateWorkout");
                    int pos = data.getIntExtra("position", -1);
                    // 更新列表中的对应项目
                    workoutItems.set(pos, updateWorkout);
                    // 通知适配器该位置的项目已changed
                    adapter.notifyItemChanged(pos);
                }
            }
        }
    }

    //全部跟新数据，效率山有低。
    private void refreshWorkoutItems() {
        workoutItems.clear(); // 清除旧数据
        workoutItems.addAll(dbHelper.getAllWorkoutItems()); // 从数据库重新加载数据
        adapter.notifyDataSetChanged(); // 通知适配器数据已更改
    }

    //for test
//    private void initWorkoutItems() {
//        workoutItems = new ArrayList<>();
//        workoutItems.add(new WorkoutItem("跑步", "提高心肺功能", R.drawable.ic_running));
//        workoutItems.add(new WorkoutItem("搏击", "增强力量", R.drawable.ic_boxing));
////        workoutItems.add(new WorkoutItem("塑性", "塑造完美身材", R.drawable.ic_bodybuilding));
//        workoutItems.add(new WorkoutItem("瑜伽", "提高柔韧性", R.drawable.ic_yoga));
//        workoutItems.add(new WorkoutItem("舞蹈", "快乐健身", R.drawable.ic_dance));
//    }
}

