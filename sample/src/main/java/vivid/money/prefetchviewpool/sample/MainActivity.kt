package vivid.money.prefetchviewpool.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import vivid.money.prefetchviewpool.core.bindToLifecycle
import vivid.money.prefetchviewpool.coroutines.setupWithPrefetchViewPool
import vivid.money.prefetchviewpool.sample.databinding.ItemTvBinding

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fl1 = FrameLayout(applicationContext)
        val fl2 = FrameLayout(this)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val adapter = Adapter()
        recycler.adapter = adapter
        adapter.submitList(listOf(Unit))

        handler.postDelayed(500) {
            adapter.submitList(List(200) { "Fake item #$it" })
        }

        recycler
            .setupWithPrefetchViewPool { setPrefetchBound(viewType = 1, count = 20) }
            .bindToLifecycle(lifecycleOwner = this)
    }
}

class Adapter : ListAdapter<Any, RecyclerView.ViewHolder>(InvalidateUtilCallback()) {

    var countHolder = 0

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is Unit -> 0
        else -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        0 -> {
            val progressView = parent.inflate(R.layout.item_pb)
            object : RecyclerView.ViewHolder(progressView) {}
        }
        1 -> {
            countHolder ++
            Log.d("CountHolder", "onCreateViewHolder: $countHolder")
            val view = ItemTvBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            DemoViewHolder(view.root)

//            val textView = parent.inflate(R.layout.item_tv)
//            object : RecyclerView.ViewHolder(textView) {}
        }
        else -> error("error")
    }

    private fun ViewGroup.inflate(layoutResId: Int): View {
        return LayoutInflater.from(context).inflate(layoutResId, this, false)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DemoViewHolder) {
            holder.bindData(getItem(position).toString())
        }
    }
}

class DemoViewHolder(private val view: View) : RecyclerView.ViewHolder(view){

    fun bindData(content: String){
        if (view is TextView){
            view.text = content
        }
    }
}

class InvalidateUtilCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return false
    }
}