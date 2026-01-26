// OpenCVApplication.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "common.h"
#include <opencv2/core/utils/logger.hpp>

wchar_t* projectPath;

void testOpenImage()
{
	char fname[MAX_PATH];
	while(openFileDlg(fname))
	{
		Mat src;
		src = imread(fname);
		imshow("image",src);
		waitKey();
	}
}

void testOpenImagesFld()
{
	char folderName[MAX_PATH];
	if (openFolderDlg(folderName)==0)
		return;
	char fname[MAX_PATH];
	FileGetter fg(folderName,"bmp");
	while(fg.getNextAbsFile(fname))
	{
		Mat src;
		src = imread(fname);
		imshow(fg.getFoundFileName(),src);
		if (waitKey()==27) //ESC pressed
			break;
	}
}

void testImageOpenAndSave()
{
	_wchdir(projectPath);

	Mat src, dst;

	src = imread("Images/Lena_24bits.bmp", IMREAD_COLOR);	// Read the image

	if (!src.data)	// Check for invalid input
	{
		printf("Could not open or find the image\n");
		return;
	}

	// Get the image resolution
	Size src_size = Size(src.cols, src.rows);

	// Display window
	const char* WIN_SRC = "Src"; //window for the source image
	namedWindow(WIN_SRC, WINDOW_AUTOSIZE);
	moveWindow(WIN_SRC, 0, 0);

	const char* WIN_DST = "Dst"; //window for the destination (processed) image
	namedWindow(WIN_DST, WINDOW_AUTOSIZE);
	moveWindow(WIN_DST, src_size.width + 10, 0);

	cvtColor(src, dst, COLOR_BGR2GRAY); //converts the source image to a grayscale one

	imwrite("Images/Lena_24bits_gray.bmp", dst); //writes the destination to file

	imshow(WIN_SRC, src);
	imshow(WIN_DST, dst);

	waitKey(0);
}

void testNegativeImage()
{
	char fname[MAX_PATH];
	while(openFileDlg(fname))
	{
		double t = (double)getTickCount(); // Get the current time [s]
		
		Mat src = imread(fname,IMREAD_GRAYSCALE);
		int height = src.rows;
		int width = src.cols;
		Mat dst = Mat(height,width,CV_8UC1);
		// Accessing individual pixels in an 8 bits/pixel image
		// Inefficient way -> slow
		for (int i=0; i<height; i++)
		{
			for (int j=0; j<width; j++)
			{
				uchar val = src.at<uchar>(i,j);
				uchar neg = 255 - val;
				dst.at<uchar>(i,j) = neg;
			}
		}

		// Get the current time again and compute the time difference [s]
		t = ((double)getTickCount() - t) / getTickFrequency();
		// Print (in the console window) the processing time in [ms] 
		printf("Time = %.3f [ms]\n", t * 1000);

		imshow("input image",src);
		imshow("negative image",dst);
		waitKey();
	}
}

void testNegativeImageFast()
{
	char fname[MAX_PATH];
	while (openFileDlg(fname))
	{
		Mat src = imread(fname, IMREAD_GRAYSCALE);
		int height = src.rows;
		int width = src.cols;
		Mat dst = src.clone();

		double t = (double)getTickCount(); // Get the current time [s]

		// The fastest approach of accessing the pixels -> using pointers
		uchar *lpSrc = src.data;
		uchar *lpDst = dst.data;
		int w = (int) src.step; // no dword alignment is done !!!
		for (int i = 0; i<height; i++)
			for (int j = 0; j < width; j++) {
				uchar val = lpSrc[i*w + j];
				lpDst[i*w + j] = 255 - val;
			}

		// Get the current time again and compute the time difference [s]
		t = ((double)getTickCount() - t) / getTickFrequency();
		// Print (in the console window) the processing time in [ms] 
		printf("Time = %.3f [ms]\n", t * 1000);

		imshow("input image",src);
		imshow("negative image",dst);
		waitKey();
	}
}

void testColor2Gray()
{
	char fname[MAX_PATH];
	while(openFileDlg(fname))
	{
		Mat src = imread(fname);

		int height = src.rows;
		int width = src.cols;

		Mat dst = Mat(height,width,CV_8UC1);

		// Accessing individual pixels in a RGB 24 bits/pixel image
		// Inefficient way -> slow
		for (int i=0; i<height; i++)
		{
			for (int j=0; j<width; j++)
			{
				Vec3b v3 = src.at<Vec3b>(i,j);
				uchar b = v3[0];
				uchar g = v3[1];
				uchar r = v3[2];
				dst.at<uchar>(i,j) = (r+g+b)/3;
			}
		}
		
		imshow("input image",src);
		imshow("gray image",dst);
		waitKey();
	}
}

void testBGR2HSV()
{
	char fname[MAX_PATH];
	while (openFileDlg(fname))
	{
		Mat src = imread(fname);
		int height = src.rows;
		int width = src.cols;

		// HSV components
		Mat H = Mat(height, width, CV_8UC1);
		Mat S = Mat(height, width, CV_8UC1);
		Mat V = Mat(height, width, CV_8UC1);

		// Defining pointers to each matrix (8 bits/pixels) of the individual components H, S, V 
		uchar* lpH = H.data;
		uchar* lpS = S.data;
		uchar* lpV = V.data;

		Mat hsvImg;
		cvtColor(src, hsvImg, COLOR_BGR2HSV);

		// Defining the pointer to the HSV image matrix (24 bits/pixel)
		uchar* hsvDataPtr = hsvImg.data;

		for (int i = 0; i<height; i++)
		{
			for (int j = 0; j<width; j++)
			{
				int hi = i*width * 3 + j * 3;
				int gi = i*width + j;

				lpH[gi] = hsvDataPtr[hi] * 510 / 360;	// lpH = 0 .. 255
				lpS[gi] = hsvDataPtr[hi + 1];			// lpS = 0 .. 255
				lpV[gi] = hsvDataPtr[hi + 2];			// lpV = 0 .. 255
			}
		}

		imshow("input image", src);
		imshow("H", H);
		imshow("S", S);
		imshow("V", V);

		waitKey();
	}
}

void testResize()
{
	char fname[MAX_PATH];
	while(openFileDlg(fname))
	{
		Mat src;
		src = imread(fname);
		Mat dst1,dst2;
		//without interpolation
		resizeImg(src,dst1,320,false);
		//with interpolation
		resizeImg(src,dst2,320,true);
		imshow("input image",src);
		imshow("resized image (without interpolation)",dst1);
		imshow("resized image (with interpolation)",dst2);
		waitKey();
	}
}

void testCanny()
{
	char fname[MAX_PATH];
	while(openFileDlg(fname))
	{
		Mat src,dst,gauss;
		src = imread(fname,IMREAD_GRAYSCALE);
		double k = 0.4;
		int pH = 50;
		int pL = (int) k*pH;
		GaussianBlur(src, gauss, Size(5, 5), 0.8, 0.8);
		Canny(gauss,dst,pL,pH,3);
		imshow("input image",src);
		imshow("canny",dst);
		waitKey();
	}
}

void testVideoSequence()
{
	_wchdir(projectPath);

	VideoCapture cap("Videos/rubic.avi"); // off-line video from file
	//VideoCapture cap(0);	// live video from web cam
	if (!cap.isOpened()) {
		printf("Cannot open video capture device.\n");
		waitKey(0);
		return;
	}
		
	Mat edges;
	Mat frame;
	char c;

	while (cap.read(frame))
	{
		Mat grayFrame;
		cvtColor(frame, grayFrame, COLOR_BGR2GRAY);
		Canny(grayFrame,edges,40,100,3);
		imshow("source", frame);
		imshow("gray", grayFrame);
		imshow("edges", edges);
		c = waitKey(100);  // waits 100ms and advances to the next frame
		if (c == 27) {
			// press ESC to exit
			printf("ESC pressed - capture finished\n"); 
			break;  //ESC pressed
		};
	}
}


void testSnap()
{
	_wchdir(projectPath);

	VideoCapture cap(0); // open the deafult camera (i.e. the built in web cam)
	if (!cap.isOpened()) // openenig the video device failed
	{
		printf("Cannot open video capture device.\n");
		return;
	}

	Mat frame;
	char numberStr[256];
	char fileName[256];
	
	// video resolution
	Size capS = Size((int)cap.get(CAP_PROP_FRAME_WIDTH),
		(int)cap.get(CAP_PROP_FRAME_HEIGHT));

	// Display window
	const char* WIN_SRC = "Src"; //window for the source frame
	namedWindow(WIN_SRC, WINDOW_AUTOSIZE);
	moveWindow(WIN_SRC, 0, 0);

	const char* WIN_DST = "Snapped"; //window for showing the snapped frame
	namedWindow(WIN_DST, WINDOW_AUTOSIZE);
	moveWindow(WIN_DST, capS.width + 10, 0);

	char c;
	int frameNum = -1;
	int frameCount = 0;

	for (;;)
	{
		cap >> frame; // get a new frame from camera
		if (frame.empty())
		{
			printf("End of the video file\n");
			break;
		}

		++frameNum;
		
		imshow(WIN_SRC, frame);

		c = waitKey(10);  // waits a key press to advance to the next frame
		if (c == 27) {
			// press ESC to exit
			printf("ESC pressed - capture finished");
			break;  //ESC pressed
		}
		if (c == 115){ //'s' pressed - snap the image to a file
			frameCount++;
			fileName[0] = NULL;
			sprintf(numberStr, "%d", frameCount);
			strcat(fileName, "Images/A");
			strcat(fileName, numberStr);
			strcat(fileName, ".bmp");
			bool bSuccess = imwrite(fileName, frame);
			if (!bSuccess) 
			{
				printf("Error writing the snapped image\n");
			}
			else
				imshow(WIN_DST, frame);
		}
	}

}

void MyCallBackFunc(int event, int x, int y, int flags, void* param)
{
	//More examples: http://opencvexamples.blogspot.com/2014/01/detect-mouse-clicks-and-moves-on-image.html
	Mat* src = (Mat*)param;
	if (event == EVENT_LBUTTONDOWN)
		{
			printf("Pos(x,y): %d,%d  Color(RGB): %d,%d,%d\n",
				x, y,
				(int)(*src).at<Vec3b>(y, x)[2],
				(int)(*src).at<Vec3b>(y, x)[1],
				(int)(*src).at<Vec3b>(y, x)[0]);
		}
}

void testMouseClick()
{
	Mat src;
	// Read image from file 
	char fname[MAX_PATH];
	while (openFileDlg(fname))
	{
		src = imread(fname);
		//Create a window
		namedWindow("My Window", 1);

		//set the callback function for any mouse event
		setMouseCallback("My Window", MyCallBackFunc, &src);

		//show the image
		imshow("My Window", src);

		// Wait until user press some key
		waitKey(0);
	}
}

/* Histogram display function - display a histogram using bars (simlilar to L3 / Image Processing)
Input:
name - destination (output) window name
hist - pointer to the vector containing the histogram values
hist_cols - no. of bins (elements) in the histogram = histogram image width
hist_height - height of the histogram image
Call example:
showHistogram ("MyHist", hist_dir, 255, 200);
*/
void showHistogram(const std::string& name, int* hist, const int  hist_cols, const int hist_height)
{
	Mat imgHist(hist_height, hist_cols, CV_8UC3, CV_RGB(255, 255, 255)); // constructs a white image

	//computes histogram maximum
	int max_hist = 0;
	for (int i = 0; i<hist_cols; i++)
	if (hist[i] > max_hist)
		max_hist = hist[i];
	double scale = 1.0;
	scale = (double)hist_height / max_hist;
	int baseline = hist_height - 1;

	for (int x = 0; x < hist_cols; x++) {
		Point p1 = Point(x, baseline);
		Point p2 = Point(x, baseline - cvRound(hist[x] * scale));
		line(imgHist, p1, p2, CV_RGB(255, 0, 255)); // histogram bins colored in magenta
	}

	imshow(name, imgHist);
}


void detectSkyContourCuFunctiiOpenCV()
{
	char folderName[MAX_PATH];
	if (openFolderDlg(folderName) == 0)
		return;

	char fname[MAX_PATH];
	FileGetter fg(folderName, "bmp");

	while (fg.getNextAbsFile(fname))
	{
		Mat src = imread(fname);
		if (src.empty()) continue;

		Mat gray, blurred;
		cvtColor(src, gray, COLOR_BGR2GRAY);
		GaussianBlur(gray, blurred, Size(5, 5), 0.8, 0.8);
		medianBlur(blurred, blurred, 5);

		std::vector<uchar> skyPixels;
		for (int y = 0; y < 50; y++)
			for (int x = 0; x < blurred.cols; x++)
				skyPixels.push_back(blurred.at<uchar>(y, x));

		cv::Scalar meanStd, stddevStd;
		meanStdDev(skyPixels, meanStd, stddevStd);
		double mean_sky = meanStd[0];
		double stddev_sky = stddevStd[0];
		double k = 1.0;
		int adaptive_threshold = mean_sky - k * stddev_sky;
		int gradient_threshold = 15;

		std::vector<int> finalY(blurred.cols, blurred.rows - 1);

		for (int x = 0; x < blurred.cols; x++) {
			int validSkySamples = 0;
			for (int y = 0; y < 20; y++)
				if (blurred.at<uchar>(y, x) > adaptive_threshold)
					validSkySamples++;

			if (validSkySamples < 3) {
				finalY[x] = blurred.rows - 1;
				continue;
			}

			int bestY_intensity = -1;
			int bestY_gradient = -1;

			for (int y = 0; y < blurred.rows; y++) {
				if (blurred.at<uchar>(y, x) < adaptive_threshold) {
					bestY_intensity = y;
					break;
				}
			}

			for (int y = 0; y < blurred.rows - 1; y++) {
				int pix = blurred.at<uchar>(y, x);
				int next = blurred.at<uchar>(y + 1, x);
				int diff = abs(next - pix);

				if (pix > mean_sky + stddev_sky)
					continue;

				if (y + 4 >= blurred.rows) continue;
				int belowAvg = (blurred.at<uchar>(y + 2, x) + blurred.at<uchar>(y + 3, x) + blurred.at<uchar>(y + 4, x)) / 3;
				if (belowAvg > mean_sky)
					continue;

				if (diff > gradient_threshold) {
					bestY_gradient = y;
					break;
				}


			}

			if (bestY_intensity >= 0 && bestY_gradient >= 0) {
				int delta = abs(bestY_intensity - bestY_gradient);
				if (delta <= 10)
					finalY[x] = (bestY_intensity + bestY_gradient) / 2;
				else if (bestY_intensity < bestY_gradient)
					finalY[x] = bestY_intensity;
				else
					finalY[x] = bestY_gradient;
			}
			else if (bestY_intensity >= 0)
				finalY[x] = bestY_intensity;
			else if (bestY_gradient >= 0)
				finalY[x] = bestY_gradient;
			else
				finalY[x] = blurred.rows - 1;
		}

		std::vector<int> smoothY = finalY;
		for (int x = 2; x < blurred.cols - 2; x++) {
			smoothY[x] = (finalY[x - 2] + finalY[x - 1] + finalY[x] +
				finalY[x + 1] + finalY[x + 2]) / 5;
		}

		Mat overlay = src.clone();
		for (int x = 0; x < overlay.cols; x++) {
			int y = smoothY[x];
			if (y >= 0 && y < overlay.rows)
				overlay.at<Vec3b>(y, x) = Vec3b(0, 0, 255);
		}

		imshow("Hybrid Statistical Contour", overlay);
		int key = waitKey(0);
		if (key == 27) break;
	}
}

void detectSkyContourWithOpticalFlow()
{
	char folderName[MAX_PATH];
	if (openFolderDlg(folderName) == 0)
		return;

	char fname[MAX_PATH];
	FileGetter fg(folderName, "bmp");

	Mat prevGray;
	std::vector<Point2f> prevPoints;

	while (fg.getNextAbsFile(fname))
	{
		Mat src = imread(fname);
		if (src.empty()) continue;

		Mat gray, blurred;
		cvtColor(src, gray, COLOR_BGR2GRAY);
		GaussianBlur(gray, blurred, Size(5, 5), 0.8, 0.8);
		medianBlur(blurred, blurred, 5);

		std::vector<uchar> skyPixels;
		for (int y = 0; y < 50; y++)
			for (int x = 0; x < blurred.cols; x++)
				skyPixels.push_back(blurred.at<uchar>(y, x));

		Scalar meanStd, stddevStd;
		meanStdDev(skyPixels, meanStd, stddevStd);
		double mean_sky = meanStd[0];
		double stddev_sky = stddevStd[0];
		double k = 1.0;
		int adaptive_threshold = mean_sky - k * stddev_sky;
		int gradient_threshold = 15;

		std::vector<int> finalY(blurred.cols, blurred.rows - 1);

		for (int x = 0; x < blurred.cols; x++) {
			int validSkySamples = 0;
			for (int y = 0; y < 20; y++)
				if (blurred.at<uchar>(y, x) > adaptive_threshold)
					validSkySamples++;

			if (validSkySamples < 3) {
				finalY[x] = blurred.rows - 1;
				continue;
			}

			int bestY_intensity = -1, bestY_gradient = -1;

			for (int y = 0; y < blurred.rows; y++) {
				if (blurred.at<uchar>(y, x) < adaptive_threshold) {
					bestY_intensity = y;
					break;
				}
			}

			for (int y = 0; y < blurred.rows - 1; y++) {
				int pix = blurred.at<uchar>(y, x);
				int next = blurred.at<uchar>(y + 1, x);
				int diff = abs(next - pix);

				if (pix > mean_sky + stddev_sky) continue;
				if (y + 4 >= blurred.rows) continue;
				int belowAvg = (blurred.at<uchar>(y + 2, x) + blurred.at<uchar>(y + 3, x) + blurred.at<uchar>(y + 4, x)) / 3;
				if (belowAvg > mean_sky) continue;

				if (diff > gradient_threshold) {
					bestY_gradient = y;
					break;
				}
			}

			if (bestY_intensity >= 0 && bestY_gradient >= 0) {
				int delta = abs(bestY_intensity - bestY_gradient);
				if (delta <= 10)
					finalY[x] = (bestY_intensity + bestY_gradient) / 2;
				else if (bestY_intensity < bestY_gradient)
					finalY[x] = bestY_intensity;
				else
					finalY[x] = bestY_gradient;
			}
			else if (bestY_intensity >= 0)
				finalY[x] = bestY_intensity;
			else if (bestY_gradient >= 0)
				finalY[x] = bestY_gradient;
			else
				finalY[x] = blurred.rows - 1;
		}

		std::vector<int> smoothY = finalY;
		for (int x = 2; x < blurred.cols - 2; x++) {
			smoothY[x] = (finalY[x - 2] + finalY[x - 1] + finalY[x] +
				finalY[x + 1] + finalY[x + 2]) / 5;
		}

		std::vector<Point2f> currentPoints;
		for (int x = 0; x < blurred.cols; x += 10) {
			int y = smoothY[x];
			currentPoints.push_back(Point2f((float)x, (float)y));
		}


		Mat overlay = src.clone();
		for (int x = 0; x < blurred.cols; x++) {
			int y = smoothY[x];
			if (y >= 0 && y < overlay.rows)
				overlay.at<Vec3b>(y, x) = Vec3b(0, 0, 255);  // red line
		}

		if (!prevGray.empty() && !prevPoints.empty()) {
			std::vector<Point2f> nextPoints;
			std::vector<uchar> status;
			std::vector<float> err;

			calcOpticalFlowPyrLK(prevGray, gray, prevPoints, nextPoints, status, err);

			for (size_t i = 0; i < nextPoints.size(); ++i) {
				if (status[i]) {
					Point p1 = prevPoints[i];
					Point p2 = nextPoints[i];

					line(overlay, p1, p2, Scalar(255, 0, 0), 1); // blue arrow
					circle(overlay, p2, 2, Scalar(255, 0, 0), FILLED); // blue dot
				}
			}
		}

		imshow("Hybrid Contour with LK Tracking (correct)", overlay);
		if (waitKey(0) == 27) break;

		prevGray = gray.clone();
		prevPoints = currentPoints;
	}
}

void detectSkyContourWithOpticalFlow_Corrected()
{
	char folderName[MAX_PATH];
	if (openFolderDlg(folderName) == 0)
		return;

	char fname[MAX_PATH];
	FileGetter fg(folderName, "bmp");

	Mat prevGray;
	std::vector<Point2f> prevPoints;

	while (fg.getNextAbsFile(fname))
	{
		Mat src = imread(fname);
		if (src.empty()) continue;

		Mat gray, blurred;
		cvtColor(src, gray, COLOR_BGR2GRAY);

		blurred = gray.clone();
		int ksize = 5, radius = ksize / 2;
		double sigma = 0.8;
		std::vector<double> kernel(ksize);
		double sum = 0;
		for (int i = -radius; i <= radius; ++i) {
			kernel[i + radius] = exp(-(i * i) / (2 * sigma * sigma));
			sum += kernel[i + radius];
		}
		for (int i = 0; i < ksize; ++i) kernel[i] /= sum;
		Mat temp = blurred.clone();
		for (int y = 0; y < blurred.rows; ++y)
			for (int x = 0; x < blurred.cols; ++x) {
				double val = 0;
				for (int k = -radius; k <= radius; ++k) {
					int idx = x + k;
					int xx = (idx < 0) ? 0 : ((idx > blurred.cols - 1) ? (blurred.cols - 1) : idx);
					val += blurred.at<uchar>(y, xx) * kernel[k + radius];
				}
				temp.at<uchar>(y, x) = uchar(val);
			}
		for (int y = 0; y < blurred.rows; ++y)
			for (int x = 0; x < blurred.cols; ++x) {
				double val = 0;
				for (int k = -radius; k <= radius; ++k) {
					int idy = y + k;
					int yy = (idy < 0) ? 0 : ((idy > blurred.rows - 1) ? (blurred.rows - 1) : idy);
					val += temp.at<uchar>(yy, x) * kernel[k + radius];
				}
				blurred.at<uchar>(y, x) = uchar(val);
			}

		Mat blurredMed = blurred.clone();
		int mksize = 5, mradius = mksize / 2;
		std::vector<uchar> window;
		window.reserve(mksize * mksize);
		for (int y = 0; y < blurred.rows; ++y)
			for (int x = 0; x < blurred.cols; ++x) {
				window.clear();
				for (int dy = -mradius; dy <= mradius; ++dy) {
					int idy = y + dy;
					int yy = (idy < 0) ? 0 : ((idy > blurred.rows - 1) ? (blurred.rows - 1) : idy);
					for (int dx = -mradius; dx <= mradius; ++dx) {
						int idx = x + dx;
						int xx = (idx < 0) ? 0 : ((idx > blurred.cols - 1) ? (blurred.cols - 1) : idx);
						window.push_back(blurred.at<uchar>(yy, xx));
					}
				}
				std::nth_element(window.begin(), window.begin() + window.size() / 2, window.end());
				blurredMed.at<uchar>(y, x) = window[window.size() / 2];
			}
		blurred = blurredMed;

		std::vector<uchar> skyPixels;
		for (int y = 0; y < 50; y++)
			for (int x = 0; x < blurred.cols; x++)
				skyPixels.push_back(blurred.at<uchar>(y, x));

		double mean_sky = 0, stddev_sky = 0;
		{
			double sum = 0, sum2 = 0;
			for (size_t i = 0; i < skyPixels.size(); ++i) sum += skyPixels[i];
			mean_sky = sum / skyPixels.size();
			for (size_t i = 0; i < skyPixels.size(); ++i) sum2 += (skyPixels[i] - mean_sky) * (skyPixels[i] - mean_sky);
			stddev_sky = sqrt(sum2 / skyPixels.size());
		}

		double k = 1.0;
		int adaptive_threshold = (int)(mean_sky - k * stddev_sky);
		int gradient_threshold = 15;

		std::vector<int> finalY(blurred.cols, blurred.rows - 1);

		for (int x = 0; x < blurred.cols; x++) {
			int validSkySamples = 0;
			for (int y = 0; y < 20; y++)
				if (blurred.at<uchar>(y, x) > adaptive_threshold)
					validSkySamples++;

			if (validSkySamples < 3) {
				finalY[x] = blurred.rows - 1;
				continue;
			}

			int bestY_intensity = -1, bestY_gradient = -1;

			for (int y = 0; y < blurred.rows; y++) {
				if (blurred.at<uchar>(y, x) < adaptive_threshold) {
					bestY_intensity = y;
					break;
				}
			}
			for (int y = 0; y < blurred.rows - 1; y++) {
				int pix = blurred.at<uchar>(y, x);
				int next = blurred.at<uchar>(y + 1, x);
				int diff = abs(next - pix);

				if (pix > mean_sky + stddev_sky) continue;
				if (y + 4 >= blurred.rows) continue;
				int belowAvg = (blurred.at<uchar>(y + 2, x) + blurred.at<uchar>(y + 3, x) + blurred.at<uchar>(y + 4, x)) / 3;
				if (belowAvg > mean_sky) continue;

				if (diff > gradient_threshold) {
					bestY_gradient = y;
					break;
				}
			}
			if (bestY_intensity >= 0 && bestY_gradient >= 0) {
				int delta = abs(bestY_intensity - bestY_gradient);
				if (delta <= 10)
					finalY[x] = (bestY_intensity + bestY_gradient) / 2;
				else if (bestY_intensity < bestY_gradient)
					finalY[x] = bestY_intensity;
				else
					finalY[x] = bestY_gradient;
			}
			else if (bestY_intensity >= 0)
				finalY[x] = bestY_intensity;
			else if (bestY_gradient >= 0)
				finalY[x] = bestY_gradient;
			else
				finalY[x] = blurred.rows - 1;
		}

		std::vector<int> smoothY = finalY;
		for (int x = 2; x < blurred.cols - 2; x++) {
			smoothY[x] = (finalY[x - 2] + finalY[x - 1] + finalY[x] +
				finalY[x + 1] + finalY[x + 2]) / 5;
		}

		std::vector<Point2f> currentPoints;
		for (int x = 0; x < blurred.cols; x += 10) {
			int y = smoothY[x];
			currentPoints.push_back(Point2f((float)x, (float)y));
		}

		Mat overlay = src.clone();
		for (int x = 0; x < blurred.cols; x++) {
			int y = smoothY[x];
			if (y >= 0 && y < overlay.rows)
				overlay.at<Vec3b>(y, x) = Vec3b(0, 0, 255);  // red line
		}

		if (!prevGray.empty() && !prevPoints.empty()) {
			std::vector<Point2f> nextPoints;
			std::vector<uchar> status;
			std::vector<float> err;

			Size winSize = Size(21, 21);
			int maxLevel = 3;
			TermCriteria criteria = TermCriteria(TermCriteria::COUNT + TermCriteria::EPS, 20, 0.03);
			calcOpticalFlowPyrLK(prevGray, gray, prevPoints, nextPoints, status, err, winSize, maxLevel, criteria);

			for (size_t i = 0; i < nextPoints.size(); ++i) {
				if (status[i]) {
					Point p1 = prevPoints[i];
					Point p2 = nextPoints[i];

					line(overlay, p1, p2, Scalar(255, 0, 0), 1); // blue arrow
					circle(overlay, p2, 2, Scalar(255, 0, 0), FILLED); // blue dot
				}
			}
		}

		imshow("Hybrid Contour with LK Tracking", overlay);
		if (waitKey(0) == 27) break;

		prevGray = gray.clone();
		prevPoints = currentPoints;
	}
}

int main() 
{
	cv::utils::logging::setLogLevel(cv::utils::logging::LOG_LEVEL_FATAL);
    projectPath = _wgetcwd(0, 0);

	int op;
	do
	{
		system("cls");
		destroyAllWindows();
		printf("Menu:\n");
		printf(" 1 - Open image\n");
		printf(" 2 - Open BMP images from folder\n");
		printf(" 3 - Image negative\n");
		printf(" 4 - Image negative (fast)\n");
		printf(" 5 - BGR->Gray\n");
		printf(" 6 - BGR->Gray (fast, save result to disk) \n");
		printf(" 7 - BGR->HSV\n");
		printf(" 8 - Resize image\n");
		printf(" 9 - Canny edge detection\n");
		printf(" 10 - Edges in a video sequence\n");
		printf(" 11 - Snap frame from live video\n");
		printf(" 12 - Mouse callback demo\n");
		printf(" 13 - Detect sky contour using hybrid statistical method ussing opencv\n");
		printf(" 14 - Detect sky contour using optical flow ussing opencv\n");
		printf(" 15 - Detect sky contour using optical flow final\n");

		printf(" 0 - Exit\n\n");
		printf("Option: ");
		scanf("%d",&op);
		switch (op)
		{
			case 1:
				testOpenImage();
				break;
			case 2:
				testOpenImagesFld();
				break;
			case 3:
				testNegativeImage();
				break;
			case 4:
				testNegativeImageFast();
				break;
			case 5:
				testColor2Gray();
				break;
			case 6:
				testImageOpenAndSave();
				break;
			case 7:
				testBGR2HSV();
				break;
			case 8:
				testResize();
				break;
			case 9:
				testCanny();
				break;
			case 10:
				testVideoSequence();
				break;
			case 11:
				testSnap();
				break;
			case 12:
				testMouseClick();
				break;
			case 13:
				detectSkyContourCuFunctiiOpenCV();
				break;
			case 14:
				detectSkyContourWithOpticalFlow();
				break;
			case 15:
				detectSkyContourWithOpticalFlow_Corrected();
				break;
		}
	}
	while (op!=0);
	return 0;
}