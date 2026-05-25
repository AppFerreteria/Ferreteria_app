package com.example.ferreteria_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import coil3.load
import coil3.request.*
class CatalogoAdapter(private var listaProductos: List<Producto>) :
    RecyclerView.Adapter<CatalogoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductoImagen: ImageView = view.findViewById(R.id.ivProductoImagen)
        val tvProductoCategoria: TextView = view.findViewById(R.id.tvProductoCategoria)
        val tvProductoNombre: TextView = view.findViewById(R.id.tvProductoNombre)
        val tvProductoPrecio: TextView = view.findViewById(R.id.tvProductoPrecio)
        val btnAgregarCarrito: MaterialButton = view.findViewById(R.id.btnAgregarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        holder.tvProductoCategoria.text = producto.categoria
        holder.tvProductoNombre.text = producto.nombre
        holder.tvProductoPrecio.text = String.format("S/ %.2f", producto.precio)

        // --- MOTOR COIL: Carga asíncrona de la imagen desde Firestore ---
        holder.ivProductoImagen.load(producto.imagenUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_martillo)
            error(R.drawable.ic_martillo)
        }
    }

    override fun getItemCount(): Int {
        return listaProductos.size
    }

    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista
        notifyDataSetChanged()
    }
}