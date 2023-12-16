package es.unex.gps.weathevent.view.buscar

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import es.unex.gps.weathevent.databinding.FragmentBuscarBinding
import es.unex.gps.weathevent.interfaces.OnCiudadClickListener
import es.unex.gps.weathevent.view.home.HomeViewModel
import kotlinx.coroutines.launch

class BuscarFragment : Fragment() {

    private val viewModel: BuscarViewModel by viewModels { BuscarViewModel.Factory }
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var listener: OnCiudadClickListener

    private lateinit var binding: FragmentBuscarBinding
    private lateinit var adapter: BuscarAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnCiudadClickListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCiudadClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBuscarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()

        // show the spinner when [spinner] is true
        viewModel.spinner.observe(viewLifecycleOwner) { show ->
            binding.spinner.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Show a Toast whenever the [toast] is updated a non-null value
        viewModel.toast.observe(viewLifecycleOwner) { text ->
            text?.let {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }

        // Configurar el EditText para filtrar la RecyclerView
        binding.editText.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.query.value = s.toString()
            }
        })

        subscribeUi(adapter)
    }

    private fun subscribeUi(adapter: BuscarAdapter) {
        viewModel.ciudadesFiltered.observe(viewLifecycleOwner) {
            ciudades -> adapter.updateData(ciudades)
        }
    }

    private fun setUpRecyclerView() {
        adapter = BuscarAdapter(
            ciudades = emptyList(),
            onFavoriteClick = {
                val fav = viewModel.changeFavorite(it)

                lifecycleScope.launch {
                    if (fav) {
                        Toast.makeText(context, "${it.name} añadido a favoritos", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "${it.name} eliminado de favoritos", Toast.LENGTH_SHORT)
                    }
                }

                return@BuscarAdapter fav
            },
            onCiudadClick = {
                listener.onCiudadClick(it)
            },
            onFavoriteLoad = {
                viewModel.checkFavorite(it)
            }
        )

        with(binding) {
            rvCiudades.layoutManager = LinearLayoutManager(context)
            rvCiudades.adapter = adapter
        }

        Log.d("DiscoverFragment", "setUpRecyclerView")
    }
}