package org.telegram.messenger;

import java.util.ArrayList;
import org.telegram.ui.PhotoViewer;

class MediaController$24 implements Runnable {
    final /* synthetic */ MediaController$AlbumEntry val$allMediaAlbumFinal;
    final /* synthetic */ MediaController$AlbumEntry val$allPhotosAlbumFinal;
    final /* synthetic */ Integer val$cameraAlbumIdFinal;
    final /* synthetic */ int val$guid;
    final /* synthetic */ ArrayList val$mediaAlbumsSorted;
    final /* synthetic */ ArrayList val$photoAlbumsSorted;

    MediaController$24(int i, ArrayList arrayList, ArrayList arrayList2, Integer num, MediaController$AlbumEntry mediaController$AlbumEntry, MediaController$AlbumEntry mediaController$AlbumEntry2) {
        this.val$guid = i;
        this.val$mediaAlbumsSorted = arrayList;
        this.val$photoAlbumsSorted = arrayList2;
        this.val$cameraAlbumIdFinal = num;
        this.val$allMediaAlbumFinal = mediaController$AlbumEntry;
        this.val$allPhotosAlbumFinal = mediaController$AlbumEntry2;
    }

    public void run() {
        if (PhotoViewer.getInstance().isVisible()) {
            MediaController.access$6600(this.val$guid, this.val$mediaAlbumsSorted, this.val$photoAlbumsSorted, this.val$cameraAlbumIdFinal, this.val$allMediaAlbumFinal, this.val$allPhotosAlbumFinal, 1000);
            return;
        }
        MediaController.access$6702(null);
        MediaController.allPhotosAlbumEntry = this.val$allPhotosAlbumFinal;
        MediaController.allMediaAlbumEntry = this.val$allMediaAlbumFinal;
        for (int a = 0; a < 3; a++) {
            NotificationCenter.getInstance(a).postNotificationName(NotificationCenter.albumsDidLoaded, Integer.valueOf(this.val$guid), this.val$mediaAlbumsSorted, this.val$photoAlbumsSorted, this.val$cameraAlbumIdFinal);
        }
    }
}
