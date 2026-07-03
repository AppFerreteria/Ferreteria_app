package com.example.ferreteria_app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade

class CatalogoAdapter(
    private var listaProductos: List<Producto>,
    private val onAgregarAlCarrito: (Producto) -> Unit
) :
    RecyclerView.Adapter<CatalogoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductoImagen: ImageView = view.findViewById(R.id.ivProductoImagen)
        val tvProductoCategoria: TextView = view.findViewById(R.id.tvProductoCategoria)
        val tvProductoNombre: TextView = view.findViewById(R.id.tvProductoNombre)
        val tvProductoStock: TextView = view.findViewById(R.id.tvProductoStock) // Enlace del nuevo componente
        val tvProductoPrecio: TextView = view.findViewById(R.id.tvProductoPrecio)
        val btnAccion: View = view.findViewById(R.id.btnAgregarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        // Carga de la información base
        holder.tvProductoCategoria.text = producto.categoria
        holder.tvProductoNombre.text = producto.nombre
        holder.tvProductoPrecio.text = holder.itemView.context.getString(R.string.formato_precio, producto.precio)

        // Carga de imagen asíncrona
        holder.ivProductoImagen.load(producto.imagenUrl) {
            crossfade(true)
        }

        if (producto.stock <= 0) {
            holder.tvProductoStock.text = holder.itemView.context.getString(R.string.estado_agotado)
            holder.tvProductoStock.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.red_error))

            // Atenuación
            holder.itemView.alpha = 0.5f
            holder.btnAccion.isEnabled = false
            holder.btnAccion.isClickable = false
            holder.itemView.setOnClickListener(null)
        } else {

            holder.tvProductoStock.text = holder.itemView.context.getString(R.string.formato_stock, producto.stock)
            holder.tvProductoStock.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_gray))


            holder.itemView.alpha = 1.0f
            holder.btnAccion.isEnabled = true
            holder.btnAccion.isClickable = true

            holder.btnAccion.setOnClickListener {
                onAgregarAlCarrito(producto)
            }
        }
    }

    override fun getItemCount(): Int = listaProductos.size

    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista
        notifyDataSetChanged()
    }
}