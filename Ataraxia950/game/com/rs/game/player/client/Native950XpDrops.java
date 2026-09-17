package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.game.player.Player;
import java.security.MessageDigest;

/** The paired 950 cache's own floating XP renderer, driven by ordinary UPDATE_STAT. */
public final class Native950XpDrops {
    public static final int INTERFACE = 1213, ROOT = 1477, ATTACH = 668, WRAPPER = 666;
    private static final class Pin {
        final int index, group, file; final String hash;
        Pin(int index, int group, int file, String hash) {
            this.index=index; this.group=group; this.file=file; this.hash=hash;
        }
    }
    private static final Pin[] PINS = {
        new Pin(3, 1213, 0, "d2229e72f2f044f8416c5309869cf83cd3bfb92cf8076403a9540b0b0a9910e2"),
        new Pin(3, 1213, 1, "216b85ef4f7faf087b847ffbb62918f457b55fca1a78ad63d09c8863a7b9e73a"),
        new Pin(3, 1213, 2, "7dad846f6134c5176d691f31d4088f68ae0e167b1760f2c2c8b8d1dc812b4c5d"),
        new Pin(3, 1213, 3, "917514cb26f3674a0f2f3d837daf990caa5e04bc3b31f37ebec55210ebfaf6cd"),
        new Pin(3, 1213, 4, "f8550a130e615205d3188bed98539850ff401a46aed480a3c374d49e60598046"),
        new Pin(3, 1213, 5, "c068ce464ae08448fd21dba37875a2cf405818e0e8881283108cc0c54f6774b3"),
        new Pin(3, 1213, 6, "e6ea03a0e56f1357f14a688b76c1c99e536400b234d856d2849de0155b7707bd"),
        new Pin(3, 1213, 7, "e3a4d938126cb58804bf878ba1a3d0918856c8faa8feda66214f9c0a6713a46b"),
        new Pin(3, 1213, 8, "ef93920755f69519002d89c95134e30f894d27f57ce94fa381dfd8430079a181"),
        new Pin(3, 1213, 9, "7a68fd959ab230fadaeaabc5f217b35d68018e1c7a7f4bd9351c82033924d6b1"),
        new Pin(3, 1213, 10, "9d1a80efd047740060fc1a6a01e3b4fcff86ff473eace4d2bbae6729c0efd955"),
        new Pin(3, 1213, 11, "03490ae5293b24f4e15da72c8eb37d25a857ee3024d71f441fddf07ffe2bbcb5"),
        new Pin(3, 1213, 12, "94f9390ef4339200864a7bacc257c3dbb202ff421ee6a99af2e17875dcd0858e"),
        new Pin(3, 1213, 13, "4db241113f4981a7bc7e2b762b6dac64b6fa23881c4b83585e68ecb46a27261f"),
        new Pin(3, 1213, 14, "661c5e684bc0d19f8f7cfe9f8db7b8b7960d0e127dc5e93283e4cef6ae1a4019"),
        new Pin(3, 1213, 15, "72fae5f2cbb2b66195e0dca18745c66981624995dc9ac52f0eb387a5305ea08a"),
        new Pin(3, 1213, 16, "84fc7fe32eab90e413be3d1c7d102b37e451b4fbf0e47ebe6c49d3e066a101bf"),
        new Pin(3, 1213, 17, "7e9e267f1f5173881af1180d45a6480c1e191011b8830a8a061676b32086eb1b"),
        new Pin(3, 1213, 18, "6587eb841ee7eac52ca1bf6fb12e6929794a17ae077de7c0a41ad3a5fa6cc335"),
        new Pin(3, 1213, 19, "67775c294cf39f9fbcdd09075ac1a85ad97fb66b2c409ed859028b7ba8151f0e"),
        new Pin(3, 1213, 20, "430b2cde3c55c58c3e890c50f1189acb266e9499e40319f0d4f4c3b9bd813c7b"),
        new Pin(3, 1213, 21, "2f983d1b8b1e6d79d6468f68c1c58187214fc4a072118767992ad8c055a2883a"),
        new Pin(3, 1213, 22, "d5df8848679156ea8e465a148bf5a0ddcbaa8d0b1d3620f0b3ae13090c7002ff"),
        new Pin(3, 1213, 23, "9cff7d899a9fede22a111fff676ab5bba590b19f891f67b8ae06ed14c13df5cd"),
        new Pin(3, 1213, 24, "c7d8496cd1fa35e260fb98b297f928d347cc78713e8eee97589f970f76eb9ba6"),
        new Pin(3, 1213, 25, "d8f538d92ad255729563009737f057d9c927bce5db8ef495f009ae1013bbaa8e"),
        new Pin(3, 1213, 26, "c04db6f008a372244d40273f921b263f5c6dcec6e4909d0ed69720d9c440c71b"),
        new Pin(3, 1213, 27, "6eafa16e33180eaf3c76aaa7d6870b7e4a3e10ef69b07310cac729eaf489ad85"),
        new Pin(3, 1213, 28, "25de8c1f23894213fb8f7e3ea74196df2d8d96f2478c42376aee12dd68005c8b"),
        new Pin(3, 1213, 29, "04c25ca05ffe4043e26c248354221793cc740117daa03d764063a4d86fb39d21"),
        new Pin(3, 1213, 30, "a987bf404041ea6be2d493d56d0f07d5512c32e79ff43b9a185da4d4441edb4e"),
        new Pin(3, 1213, 31, "cb0e428d04dec4edfa4bf7cd18ea804c9447adf97c6ba1f864b56f3d80fc6723"),
        new Pin(3, 1213, 32, "346a43e50746e2a8d77c775750bf90efd00a2ec241e752ed6359b26be03677d0"),
        new Pin(3, 1213, 33, "1fb8a9ed3fc9bdebb1d5985902e3afba4b13a156341ce7141a490988c59ca868"),
        new Pin(3, 1213, 34, "3b182dad67ebc48f7f74f003dfae3896a40729e4cb9a5f164a713ed136c20683"),
        new Pin(3, 1213, 35, "d3978f976ae4bade51d2f4431cce1e82ed1e68aee30675946c4bd266d6e649f6"),
        new Pin(3, 1213, 36, "5b3a39dc519bfe85b335434fa68e89ad742a0b80907bc36a173e6ad76268d31c"),
        new Pin(3, 1213, 37, "e362b5124db5f13415d9c8b8f6cb066724c642d86e2db8c2516de289b684d84d"),
        new Pin(3, 1213, 38, "403f627032de147a2feb77ce1d1b58e2c75dfa02868663a137665c6033303f02"),
        new Pin(3, 1213, 39, "c79e0d8e6783932292e3310a50b3252b63ed94056e208370d29bf200f3d371a6"),
        new Pin(3, 1213, 40, "53da83ecdf6dd1c1d86d743eabe7605d50c50e7b9336ebb12428a6b5945f8e3c"),
        new Pin(3, 1213, 41, "2f60cb0f6f663db565912a5d217456355b73a3b24b8439b2a2c75c49fdaf2972"),
        new Pin(3, 1213, 42, "d0395cd306e659a0dc6515a5fb9a6631cd6407e8a7c0783c907930ddc88b518e"),
        new Pin(3, 1213, 43, "e30d1e934cb606141d4c8c0904c0d91b491fe3c93a89a09bdd2f391944f64048"),
        new Pin(3, 1213, 44, "668867f939a550c0d826be2688f091cb62ae7a7d951e085f7f2fdfab109366a4"),
        new Pin(3, 1213, 45, "bf671b9f925a46c816d0016e286e028d3d1808207765cafb6591ba2a8a78281c"),
        new Pin(3, 1213, 46, "13a19f7224ca07a47f94e1f0c052a6b49b6890786d53ba03c3beec7d6769c200"),
        new Pin(3, 1213, 47, "85f90f9eb28ab8f9d8183e2fc510b04bd853bf0aa5aea0774dd8ab980c347f3d"),
        new Pin(3, 1213, 48, "1b79332f2a92d0e3d5b33a85801cd2e3b68df4bc4960682a33eee91fd17ef220"),
        new Pin(3, 1213, 49, "ddceed32c6a6bbc85f916aebbb1b4a97e7bc87301c734a3cc6ef4f907f6f2ec8"),
        new Pin(3, 1213, 50, "f4c7dd7ff6376d13044844b38612ac4a8991f85764c7b6625a42c883f0e052bc"),
        new Pin(3, 1213, 51, "daccd948c5208af577afbc08a48057cd2f71b665e8e449524b7db451dc1e5c07"),
        new Pin(3, 1213, 52, "857f3d1c7e046de81ed1814428778e51ad477f1a40e12a165e243b4366308bf2"),
        new Pin(3, 1213, 53, "20cb83623aa095b46a782c6e4943202cb2839d2c7da4366c2f9ac08d4c8a151f"),
        new Pin(3, 1213, 54, "3cce621a0973b866f34e0b0950f68fafe33d15d4848ea17272eb80c70a2d286f"),
        new Pin(3, 1213, 55, "173f270d36ae25be4705b081f5f931f34b40bc5db47571f69851a94edc940db9"),
        new Pin(3, 1213, 56, "97ca1bfa79b23299b0104f5110f4ecbbf30c1397ceb430181eea32d577769bcd"),
        new Pin(3, 1213, 57, "862f493ce5a37a88f5c0d3aed444f0b3197fbba090d25623e223d3b2d1be58b0"),
        new Pin(3, 1213, 58, "a04a9f4e833962d4dfacc0dabd55e812e4901bd109ffe12c034b099ba80d5a0f"),
        new Pin(3, 1213, 59, "79e035d7128c668954d7b557f84a557734dd1c1b6c901f1e66da8730700d8219"),
        new Pin(3, 1213, 60, "68e67c4afd3e5d0f959116f3c41c081da7157bd950b6ec124457f91784d024a8"),
        new Pin(3, 1213, 61, "f79db3ceb593767eea8d714f8601753e07a2070a1442b383c5b617b30e2ed266"),
        new Pin(3, 1213, 62, "2710f5c2cfb9a632773cdce6a558aceecd50653b710bfe843b9ac4adce2acbdf"),
        new Pin(3, 1213, 63, "6bd72d29f329e500b005667aa160652de642db54dde4ef740be66f07647c0dff"),
        new Pin(3, 1213, 64, "44fe90ac3aa0f9d9d7826940cee3646d6e643d9d0bb6b9a56acc84e198c5ef84"),
        new Pin(3, 1213, 65, "e49398204dda8f8a517db7699b1c8d7c34462ea3ff96dd8dcec1decfc42af5c0"),
        new Pin(3, 1213, 66, "ae0fb39d72d871ec615654fb99919154e10ddc18fe4f1d2b578c67e4af8348b9"),
        new Pin(3, 1213, 67, "5db3593698ca0c673d0e158c0976f25cf6481451926e8647d9d28e45b8cd248c"),
        new Pin(3, 1213, 68, "9aa500656b1745000b47d31f566386b605a0bca8041b9b14821dd01d93fdfe41"),
        new Pin(3, 1213, 69, "dbeef99006a560847a39c125cc4a58812fa6cc91ca1e92e8de3543a63f7502ee"),
        new Pin(3, 1213, 70, "2f0a0caf7452a6cb50b8d3adecb8927dd70537afaabd38abcc4806f1c56e9230"),
        new Pin(3, 1213, 71, "72ab3811999505628042e5a66b7f6b54ddff830847cc95d78a03b08f864b4de8"),
        new Pin(3, 1213, 72, "d5e2432fcf694d667fefce6c84a7d20a2e4cb07210b6b9d98206a8ed0ee600b3"),
        new Pin(3, 1213, 73, "e6c248c6263c6beb1e0de09accac439270d40d6ec3acf9eb7a026718c6ad1e3d"),
        new Pin(3, 1213, 74, "4b5ec5d5d9a0bc9095a66b4eaaef346edf0ecdf0c340fb644104ed96382fdd99"),
        new Pin(3, 1213, 75, "271acb0b15eab195c8f9950fd50488c456b376b9ab4ed94b70c30526d0f38e65"),
        new Pin(3, 1213, 76, "fdbe662b46d617715244030faf80d4048527d5d8dda341345e1373b8f94ff764"),
        new Pin(3, 1213, 77, "c501dd9f28c97a8a62c550a0ba382ebd4d3585d906dac6b90dfc740b50ac250d"),
        new Pin(3, 1213, 78, "5e7b4d47275f9f8342e2b301a32c6e156ce32d4ae67569f3b8f8616bf2ebbaa2"),
        new Pin(3, 1213, 79, "6667257544c9d58007ed9bf7ef84738ed523ac3b1391ceec4407e24dd3b43bc8"),
        new Pin(3, 1213, 80, "d4c81918c3418efff150b47dc946359eee9680e1d044e556a1e2c41d4fbb5db6"),
        new Pin(3, 1213, 81, "8d79bedc0bf4c626a7c00189b56a7d99378b12bbdd5649ecb7a977e80cc86dde"),
        new Pin(3, 1213, 82, "60c843fc5c228d9b847274d2c055642d63781c1cba7ebcfa2392422e551cade4"),
        new Pin(3, 1213, 83, "4e001db507ddd808f3f56f48b838372bd41ec7664225f0c70d32bedbd7ba25cb"),
        new Pin(3, 1213, 84, "172f20b5e9f2b4dfa9cf182e3876f2bf00621f61552518e986f9195e414f55d5"),
        new Pin(3, 1213, 85, "85ec8e580b8bbe0edad03d0e9b25c418c3ee03fe3768b969c9276a37a5ebb433"),
        new Pin(3, 1213, 86, "e7978bcedb8ad19c5aa17ea8268e6aa9034c29424d601a5651df206ba0ea601e"),
        new Pin(3, 1213, 87, "1b70273bcced4bf72c1290cffe908ddcfe87abe1494a666f72f7b8c6be346804"),
        new Pin(3, 1213, 88, "d82187763f475d6fd6ca8b3005581d03bddefe58b2a28314cd8b450478e267cf"),
        new Pin(3, 1213, 89, "3359cd222c700321dd26a364bef40efe049534663ce52ca59386cb2d3ea97b10"),
        new Pin(3, 1213, 90, "491eb079fcda848b47b156e0784b003aaae25ae2e8a1d1c69d991d49a6b121fa"),
        new Pin(3, 1213, 91, "fc2297dd3920e7df2fd514417e08519e74fd7f54c92f83f925fa743d4f30921c"),
        new Pin(3, 1213, 92, "82d8d4868a3c150689dd5b600dfe7e668e97855bda34b9edd42c48f991cea8c2"),
        new Pin(3, 1213, 93, "c96aabf2ddcea4226aaa70695285019cdef7585f148ac4643e89e7738113ac4c"),
        new Pin(3, 1213, 94, "8ccc87d359f1661d5f462440f5e31c6429b31a192253870e8c02e86eca35c33b"),
        new Pin(3, 1213, 95, "5411edb578508a1667c4379d37eb62c87d8787b49bf1693f8c1b69cd58065a3b"),
        new Pin(3, 1213, 96, "99e6123bfc39b91bb5e5e06892ea5a30f5b5b15600115bd870329524942d36cb"),
        new Pin(3, 1477, 27, "af6e31a36e133311dfedb970fe23c023efd8635d947d0541f9619c16e866c838"),
        new Pin(3, 1477, 565, "3983846203867467f02dac9eb3ef0026d251a1f54cf97ac096f56670672a7025"),
        new Pin(3, 1477, 636, "90f0050628298b65948c1e93ac6474523acc95f5ee9572bdbaf5109e23c873f0"),
        new Pin(3, 1477, 637, "700adac2bd78b2c792aa788af58aa04cd3b8c612332062375b9a150371d7aa72"),
        new Pin(3, 1477, 666, "9db652e502457fe3fad7930ad62290d2a20c7eb5eef4846ea85849d1ced6528c"),
        new Pin(3, 1477, 668, "870130985d4729c2995c6dc31934e9ba4e54f0dc453e3ef15667e03262b419e4"),
        new Pin(17, 30, 36, "63a614bce39c6953c96821e6fda3ad88ea42b5d3a6d0b8feb82819b2ce84694a"),
        new Pin(22, 941, 31, "a8f45e76493b06958daf5635b640191d2d7b466ec8862ba2b2ea681772ff2480"),
        new Pin(2, 69, 228, "2aa5c3f3802e8e6bd21ea4258c43ced4f293b4d1d8e76505f9675999c8c65309"),
        new Pin(12, 5653, 0, "5ea9fdf922da0905fd3350a38c36957915bb11a4446e677ffa5b09962a8af42d"),
        new Pin(12, 5657, 0, "54c2a23220c38a1f2a6be26c8ef0de5402a25c9b7aa5a7382024736f034d8373"),
        new Pin(12, 5658, 0, "529a4d18ac2199870afa96731a392992cb4bc5eba0c99bb2f03034de040fbe7e"),
        new Pin(12, 5659, 0, "723661d2368da5eb806a3860bdde93c9f90e8648de25a33c4fac358ea6d7add6"),
        new Pin(12, 5661, 0, "726004604e493fad6831a6c2aeb50f913a3696d93a34cc93ae8684db3ccbdfb6"),
        new Pin(12, 5662, 0, "cdc9ac1890d16f9553394dae824e98a195d742a1bd4f5c798ba2d6429fd92737"),
        new Pin(12, 5664, 0, "ddcb0b4348762b3c0028118606cb3a40d3868d34c50ac47d0d81fc34d4d0ce74"),
        new Pin(12, 12083, 0, "79e9a6276402f08f4b61ddd171a70025f1a20ce2311fd11d78518d67e1cfb66d"),
        new Pin(12, 10849, 0, "611c7a9d9412f2e9aaedc26afc37007487cb1b947088e8e8cce1003f96149ea0"),
        new Pin(12, 2680, 0, "e78d656b39c120982de5d6f9f7438d2ccc9ebafbada54bc7eb1cf04e09ad1ea7"),
        new Pin(12, 6431, 0, "df7852b69ea0887598894c0cf1abd95ca9e76682bee3b9d6323be32e576b3b1b"),
        new Pin(12, 12082, 0, "9bed20020851b7f86b32c64cc35f48b7b3a81fb9cc2c43a0c5ee5e1d54d85b6c"),
        new Pin(12, 12040, 0, "cfc9b12c0667dcf50894f5103aabf82263478a35f0db4464515b73f12945913f"),
        new Pin(12, 10888, 0, "6fb42b072cf03a0a0d835db0922b5810c6c9b0407b55d6b0ec505e921727c3b2"),
        new Pin(12, 11891, 0, "51fe3dc752217609d805db2f2b63f8bb68f363e94e33b5e11d03f2731d4dc3b0"),
        new Pin(12, 11889, 0, "1f703a5f83a1a322c6083b6b461616355a7dcc67afe613c5d9b6cc29771fdaa8"),
        new Pin(12, 4037, 0, "03fa657ce1302a9148852e02324ee351d571251cf62431053173a299713b12cf"),
        new Pin(12, 5655, 0, "7af17fbddc3234d607a4ca8c0f443c7f9f6833f19e00e29b410cb40ae18d152c"),
        new Pin(12, 2673, 0, "7061848499eae05d2c3e05a1eeb7fe5ab8a16551fa69b379db32a36877a8cdbf"),
        new Pin(12, 8002, 0, "b7a70ed90e43a120cb0b753f9f48e79d20093cfb6da7016c0552035bbcd6890b"),
        new Pin(17, 2, 169, "45ce02e8fbcb43cb474cab376e44726738644f3934fbb55eb52a6d479f64e143"),
        new Pin(17, 2, 168, "f861c6f157a95ad024dba08b7e4cd26af1c4e5098e747cf9f898ded423a31a7a"),
        new Pin(17, 42, 113, "52221674c1fb326c5d89f24a5767c3788f1580821baa4b42a1312ce5f6ab7493"),
        new Pin(12, 11145, 0, "3785303ee6b2c09c260e718b853c8ef7773bc080d07d0dc3d4a6d150b0350575"),
        new Pin(12, 13268, 0, "f1f865db2310bb0fb19337a693f97d60b89b5949c1d9b1f94fcbb3308295f18a"),
        new Pin(12, 2330, 0, "29dd977d6dc6d576c6bad1af429e48201462a0d06fcdb070ae9d08edf61f4197"),
        new Pin(12, 8707, 0, "c40ff90931165d958d9995914b4c55627f7b9a6d2cd11c21eeb0e20a47e17cd6"),
        new Pin(12, 8708, 0, "f2cdca500c552a91ba4ff11ea5b22cbd9b91c6602c3ba1be4d4264f9efa30039"),
    };
    private static Store verified;
    private Native950XpDrops() { }

    public static synchronized boolean verifyCache() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 XP popups require the paired flat cache");
        if (verified == Cache.STORE) return true;
        boolean valid = true;
        for (Pin pin : PINS) {
            byte[] bytes = Cache.STORE.getIndexes()[pin.index].getFile(pin.group, pin.file);
            valid &= NativeCacheVerification.requireBinding("950 XP popups",
                    pin.index + "/" + pin.group + "/" + pin.file, pin.hash, hash(bytes));
        }
        if (valid) verified = Cache.STORE;
        return valid;
    }

    /** Call once after the initial skill burst, so onLoad records the restored XP as baseline. */
    public static void open(Player player) {
        if (player == null || !player.isNative950() || Cache.STORE == null || !verifyCache()) return;
        emit(player);
    }

    static void emit(Player player) {
        // 1213:67 onStatTransmit -> 5661 -> 5662 computes positive skill deltas.
        // Its onLoad5658 -> 5659 -> 5653 snapshots XP. No guessed XP CS2 call.
        player.getPackets().sendInterface(true, (ROOT << 16) | ATTACH, INTERFACE);
        player.getPackets().sendHideIComponent(ROOT, WRAPPER, false);
        player.getPackets().sendHideIComponent(ROOT, ATTACH, false);
        // Restore this slot's exact cache defaults, then save the visible rectangle.
        // Wrapper666 onLoad8409(1026)->8411 participates in NIS resize/preset restores;
        // merely unhiding it can leave a previously saved zero-size/hidden rectangle.
        // 173x114 at374,46 and all four modes0 are the paired950 component header.
        int wrapper = (ROOT << 16) | WRAPPER;
        player.getPackets().sendExecuteScript(11145, 173, 114, 0, 0, wrapper);
        player.getPackets().sendExecuteScript(13268, 374, 46, 0, 0, wrapper);
        player.getPackets().sendExecuteScript(2330, wrapper);
        player.getPackets().sendExecuteScript(8707, 1026);
        player.getPackets().sendExecuteScript(8708, 1026, 8);
        // 5662 builds the floating text on637 under636, outside the1213 slot.
        player.getPackets().sendHideIComponent(ROOT, 636, false);
        player.getPackets().sendHideIComponent(ROOT, 637, false);
        player.getPackets().sendConfigByFile(228, 0);
    }

    private static String hash(byte[] bytes) {
        if (bytes == null) return "missing";
        try {
            StringBuilder value = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes))
                value.append(String.format("%02x", b & 255));
            return value.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
