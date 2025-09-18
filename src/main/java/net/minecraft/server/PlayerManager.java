package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PlayerManager {

    private final byte[] circularMask; //Poseidon Circular view shape
    private final Set<ChunkCoordIntPair> prevChunksTmp = new HashSet<>(); //Poseidon Circular view shape
    public List managedPlayers = new ArrayList();
    private PlayerList b = new PlayerList();
    private List c = new ArrayList();
    private MinecraftServer server;
    private int e;
    private int f;
    private final int[][] g = new int[][] { { 1, 0}, { 0, 1}, { -1, 0}, { 0, -1}};

    public PlayerManager(MinecraftServer minecraftserver, int i, int j) {
        if (j > 15) {
            throw new IllegalArgumentException("Too big view radius!");
        } else if (j < 3) {
            throw new IllegalArgumentException("Too small view radius!");
        } else {
            this.f = j;
            this.server = minecraftserver;
            this.e = i;
            //Poseidon Circular view-shape start
            if (!PoseidonConfig.getInstance().getConfigBoolean("world.settings.circular-view-shape.enable", false)) {
                this.circularMask = new byte[0];
            } else {
                int sideWidth = j + j + 1;
                this.circularMask = new byte[sideWidth * sideWidth];
                for (int x = 0; x < sideWidth; x++) {
                    for (int y = 0; y < sideWidth; y++) {
                        int a = x - j, b = y - j;
                        if (a * a + b * b - sideWidth < j * j) {
                            this.circularMask[sideWidth * x + y] = 1;
                        }
                    }
                }
            }
            //Poseidon Circular view-shape end
        }
    }

    public WorldServer a() {
        return this.server.getWorldServer(this.e);
    }

    public void flush() {
        for (int i = 0; i < this.c.size(); ++i) {
            ((PlayerInstance) this.c.get(i)).a();
        }

        this.c.clear();
    }

    private PlayerInstance a(int i, int j, boolean flag) {
        long k = (long) i + 2147483647L | (long) j + 2147483647L << 32;
        PlayerInstance playerinstance = (PlayerInstance) this.b.a(k);

        if (playerinstance == null && flag) {
            playerinstance = new PlayerInstance(this, i, j);
            this.b.a(k, playerinstance);
        }

        return playerinstance;
    }

    public void flagDirty(int i, int j, int k) {
        int l = i >> 4;
        int i1 = k >> 4;
        PlayerInstance playerinstance = this.a(l, i1, false);

        if (playerinstance != null) {
            playerinstance.a(i & 15, j, k & 15);
        }
    }

    public void addPlayer(EntityPlayer entityplayer) {
        int i = (int) entityplayer.locX >> 4;
        int j = (int) entityplayer.locZ >> 4;

        entityplayer.d = entityplayer.locX;
        entityplayer.e = entityplayer.locZ;
        int k = 0;
        int l = this.f;
        int i1 = 0;
        int j1 = 0;

//        this.a(i, j, true).a(entityplayer); //Poseidon Circular view-shape: Moved inside the if statement

        int k1;

        //Poseidon Circular view-shape start
        if (this.circularMask.length == 0) {
        this.a(i, j, true).a(entityplayer);
        for (k1 = 1; k1 <= l * 2; ++k1) {
            for (int l1 = 0; l1 < 2; ++l1) {
                int[] aint = this.g[k++ % 4];

                for (int i2 = 0; i2 < k1; ++i2) {
                    i1 += aint[0];
                    j1 += aint[1];
                    this.a(i + i1, j + j1, true).a(entityplayer);
                }
            }
        }

        k %= 4;

        for (k1 = 0; k1 < l * 2; ++k1) {
            i1 += this.g[k][0];
            j1 += this.g[k][1];
            this.a(i + i1, j + j1, true).a(entityplayer);
        }
        } else {
            k1 = l + l + 1;

            for (int vx = -l; vx <= l; vx++) {
                for (int vz = -l; vz <= l; vz++) {
                    int relX = vx + l, relZ = vz + l;
                    if (this.circularMask[k1 * relX + relZ] == 0) {
                        continue;
                    }

                    this.a(i + vx, j + vz, true).a(entityplayer);
                }
            }
            List<ChunkCoordIntPair> chunksToSend = entityplayer.chunkCoordIntPairQueue;
            chunksToSend.sort(new Comparator<ChunkCoordIntPair>() {
                public int compare(ChunkCoordIntPair a, ChunkCoordIntPair b) {
                    return Math.max(Math.abs(a.x - i), Math.abs(a.z - j)) - Math.max(Math.abs(b.x - i), Math.abs(b.z - j));
                }
            });
        }
        //Poseidon Circular view-shape end

        this.managedPlayers.add(entityplayer);
    }

    public void removePlayer(EntityPlayer entityplayer) {
        int i = (int) entityplayer.d >> 4;
        int j = (int) entityplayer.e >> 4;

        for (int k = i - this.f; k <= i + this.f; ++k) {
            for (int l = j - this.f; l <= j + this.f; ++l) {
                PlayerInstance playerinstance = this.a(k, l, false);

                if (playerinstance != null) {
                    playerinstance.b(entityplayer);
                }
            }
        }

        this.managedPlayers.remove(entityplayer);
    }

    private boolean a(int i, int j, int k, int l) {
        int i1 = i - k;
        int j1 = j - l;

        return i1 >= -this.f && i1 <= this.f ? j1 >= -this.f && j1 <= this.f : false;
    }

    public void movePlayer(EntityPlayer entityplayer) {
        int i = (int) entityplayer.locX >> 4;
        int j = (int) entityplayer.locZ >> 4;
        double d0 = entityplayer.d - entityplayer.locX;
        double d1 = entityplayer.e - entityplayer.locZ;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            int k = (int) entityplayer.d >> 4;
            int l = (int) entityplayer.e >> 4;
            int i1 = i - k;
            int j1 = j - l;

            if (i1 != 0 || j1 != 0) {
                //Poseidon Circular view-shape start
                if (this.circularMask.length == 0) {
                for (int k1 = i - this.f; k1 <= i + this.f; ++k1) {
                    for (int l1 = j - this.f; l1 <= j + this.f; ++l1) {
                        if (!this.a(k1, l1, k, l)) {
                            this.a(k1, l1, true).a(entityplayer);
                        }

                        if (!this.a(k1 - i1, l1 - j1, i, j)) {
                            PlayerInstance playerinstance = this.a(k1 - i1, l1 - j1, false);

                            if (playerinstance != null) {
                                playerinstance.b(entityplayer);
                            }
                        }
                    }
                }
                } else {
                    this.prevChunksTmp.clear();
                    this.prevChunksTmp.addAll(entityplayer.playerChunkCoordIntPairs);
                    int k1 = this.f + this.f + 1;

                    for (int vx = -this.f; vx <= this.f; vx++) {
                        for (int vz = -this.f; vz <= this.f; vz++) {
                            int relX = vx + this.f, relZ = vz + this.f;
                            if (this.circularMask[k1 * relX + relZ] == 0) {
                                continue;
                            }

                            if (!this.prevChunksTmp.remove(new ChunkCoordIntPair(i + vx, j + vz))) {
                                this.a(i + vx, j + vz, true).a(entityplayer);
                            }
                        }
                    }

                    for (ChunkCoordIntPair pair : this.prevChunksTmp) {
                        PlayerInstance playerinstance = this.a(pair.x, pair.z, false);

                        if (playerinstance != null) {
                            playerinstance.b(entityplayer);
                        }
                    }
                    this.prevChunksTmp.clear();
                }
                entityplayer.d = entityplayer.locX;
                entityplayer.e = entityplayer.locZ;

                // CraftBukkit start - send nearest chunks first
                if (i1 > 1 || i1 < -1 || j1 > 1 || j1 < -1) {
                    final int x = i;
                    final int z = j;
                    List<ChunkCoordIntPair> chunksToSend = entityplayer.chunkCoordIntPairQueue;

                    java.util.Collections.sort(chunksToSend, new java.util.Comparator<ChunkCoordIntPair>() {
                        public int compare(ChunkCoordIntPair a, ChunkCoordIntPair b) {
                            return Math.max(Math.abs(a.x - x), Math.abs(a.z - z)) - Math.max(Math.abs(b.x - x), Math.abs(b.z - z));
                        }
                    });
                }
                // CraftBukkit end
            }
        }
    }
    
    // Poseidon
    public boolean a(EntityPlayer entityplayer, int i, int j) {
        PlayerInstance playerchunk = this.a(i, j, false);

        return playerchunk == null ? false : PlayerInstance.b(playerchunk).contains(entityplayer) && !entityplayer.chunkCoordIntPairQueue.contains(PlayerInstance.a(playerchunk));
    }

    public int getFurthestViewableBlock() {
        return this.f * 16 - 16;
    }

    static PlayerList a(PlayerManager playermanager) {
        return playermanager.b;
    }

    static List b(PlayerManager playermanager) {
        return playermanager.c;
    }
}
