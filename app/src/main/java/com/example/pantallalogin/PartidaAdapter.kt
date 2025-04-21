package com.example.pantallalogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.database.Partida

class PartidaAdapter(private val partidas: List<Partida>)
    : RecyclerView.Adapter<PartidaAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha     : TextView = itemView.findViewById(R.id.tvItemFecha)
        val tvNumero    : TextView = itemView.findViewById(R.id.tvItemNumero)
        val tvResultado : TextView = itemView.findViewById(R.id.tvItemResultado)
        val tvSaldo     : TextView = itemView.findViewById(R.id.tvItemSaldo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partida, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partida = partidas[position]
        holder.tvFecha.text     = partida.fecha
        holder.tvNumero.text    = "Número: ${partida.numero}"
        holder.tvResultado.text = if (partida.resultado >= 0)
            "Ganaste ${partida.resultado}"
        else
            "Perdiste ${-partida.resultado}"
        holder.tvSaldo.text     = "Saldo: ${partida.saldo}"
    }

    override fun getItemCount() = partidas.size
}
