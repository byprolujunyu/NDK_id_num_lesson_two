#include <jni.h>
#include <string>
#include "utils.h"
//#include<>直接从编译器自带的函数库中寻找文件
//#include""是先从自定义的文件中找 ，如果找不到在从函数库中寻找文件
#include "common.h"
#include <opencv2/opencv.hpp>

#define DEFAULT_CARD_WIDTH 640
#define DEFAULT_CARD_HEIGHT 400
#define FIX_IDCARD_SIZE Size(DEFAULT_CARD_WIDTH,DEFAULT_CARD_HEIGHT)

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jobject JNICALL
Java_com_netease_id_1num_MainActivity_findIdNumber(JNIEnv *env, jobject instance, jobject bitmap,
                                                   jobject argb8888) {

    // TODO
    // c没有Bitmap,要将Bitmap转成Mat
    //原图
    Mat src_img;
    Mat des_img;
    //1 先将bitmap转成Mat
    bitmap2Mat(env, bitmap, &src_img);
    //3 归一化
    Mat dst;
    resize(src_img, dst, FIX_IDCARD_SIZE);
    //4 灰度化
    cvtColor(src_img, dst, COLOR_RGB2GRAY);
    //5 二值化
    threshold(dst, dst, 100, 255, THRESH_BINARY);
    //9 膨胀
    Mat erodeElement = getStructuringElement(MORPH_RECT, Size(40, 10));
    erode(dst, dst, erodeElement);
    //6 轮廓检测
    vector<vector<Point>> contours;
    vector<Rect> rects;
    findContours(dst, contours, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0));
    for (int i = 0; i < contours.size(); i++) {
        //7 获取矩形区域
        Rect rect = boundingRect(contours.at(i));
        //8  绘制矩形
//      rectangle(dst,rect,Scalar(0,0,255));
        //10 遍历这些矩形区域，去找到符合身份证号码特征的区域
        //身份证号码是拥有固定的长宽比的>1:8&&<1:16
        if (rect.width > rect.height * 8 && rect.width < rect.height * 16) {
//          rectangle(dst,rect,Scalar(0,0,255));
            //11 存储获取到的矩形区域
            rects.push_back(rect);
        }
    }
    //12 查找坐标最低的矩形区域
    int lowPoint = 0;
    Rect finalRect;
    for (int i = 0; i < rects.size(); i++) {
        Rect rect = rects.at(i);
        Point p = rect.tl();
        if (rect.tl().y > lowPoint) {
            lowPoint = rect.tl().y;
            finalRect = rects.at(i);
        }
    }
    //    rectangle(dst,finalRect,Scalar(0,0,255));
    //2 将Mat转成Bitmap
//    des_img = src_img;
    //13
    des_img = src_img(finalRect);
    return createBitmap(env, des_img, argb8888);
}