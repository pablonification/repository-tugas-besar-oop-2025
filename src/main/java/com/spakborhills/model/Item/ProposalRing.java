package com.spakborhills.model.Item;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Player;
import com.spakborhills.model.NPC.NPC;
// import com.spakborhills.model.Enum.RelationshipStatus;

public class ProposalRing extends Item {
    public ProposalRing() {
        // Item Special, tidak bisa dibeli dan dijual
        super("Proposal Ring", ItemCategory.RING, 0, 0);
    }
    /**
     * Mengimplementasikan aksi 'use' untuk Proposal Ring.
     * Aksi utamanya adalah mencoba melamar NPC yang menjadi target.
     * Metode ini sebaiknya fokus pada validasi dasar target.
     * Logika detail Proposing (cek heart points, ubah status, efek ke player/waktu)
     * lebih baik dihandle oleh Controller/Player yang memanggil metode ini.
     *
     * @param player Pemain yang menggunakan cincin.
     * @param target Objek target, diharapkan adalah NPC yang akan dilamar.
     * @return true jika target valid (NPC) dan aksi Proposing bisa dicoba, false jika target tidak valid.
     */
    @Override
    public boolean use(Player player, Object target) {
        // Validasi target adalah NPC
        if (!(target instanceof NPC)) {
            System.out.println("ERROR: Proposal Ring hanya bisa digunakan untuk melamar NPC!");
            return false;
        }
        return true;
    }

    @Override
    public Item cloneItem() {
        return new ProposalRing();
    }
}