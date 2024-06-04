package com.example.Distortion;

import java.util.Objects;

public class ImageDistortion {
	 private int distortionid;
	 
	 private String distortionclass;

	public int getDistortionid() {
		return distortionid;
	}

	public void setDistortionid(int distortionid) {
		this.distortionid = distortionid;
	}

	public String getDistortionclass() {
		return distortionclass;
	}

	public void setDistortionclass(String distortionclass) {
		this.distortionclass = distortionclass;
	}

	@Override
	public String toString() {
		return "ImageDistortion [distortionid=" + distortionid + ", distortionclass=" + distortionclass + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(distortionclass, distortionid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageDistortion other = (ImageDistortion) obj;
		return Objects.equals(distortionclass, other.distortionclass) && distortionid == other.distortionid;
	}
	 
}
