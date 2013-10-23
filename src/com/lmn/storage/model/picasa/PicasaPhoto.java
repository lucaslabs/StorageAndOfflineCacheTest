package com.lmn.storage.model.picasa;

/**
 * PicasaPhoto model.
 * 
 * @author Lucas Nobile
 */
public class PicasaPhoto {

	private String mTitle;
	private String mThumbnailUrl;

	public PicasaPhoto(String title, String thumbnailUrl) {
		this.mTitle = title;
		this.mThumbnailUrl = thumbnailUrl;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getThumbnailUrl() {
		return mThumbnailUrl;
	}

	public void setThumnailUrl(String mThumnailUrl) {
		this.mThumbnailUrl = mThumnailUrl;
	}
}
