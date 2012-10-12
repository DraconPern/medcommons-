#if defined(_MSC_VER)
#pragma warning ( disable : 4786 )
#endif


#include "itkImageFileReader.h"
#include "itkImageFileWriter.h"
#include "itkImage.h"
#include "itkRescaleIntensityImageFilter.h"
#include "itkMetaDataDictionary.h"
#include "itkMetaDataObject.h"
#include "itkGDCMImageIO.h"
#include "itkAffineTransform.h"
#include "itkImage.h"
#include "itkRGBPixel.h"
#include "itkImageRegionIterator.h"
#include "itkAffineTransform.h"
#include "itkResampleImageFilter.h"
#include "itkImageRegionIteratorWithIndex.h"
#include "itkLinearInterpolateImageFunction.h"
#include "itkExceptionObject.h"
#include "itkVector.h"
#include "itkIdentityTransform.h"
#include "itkIntensityWindowingImageFilter.h"
#include "itkVectorResampleImageFilter.h"
#include "itkVectorLinearInterpolateImageFunction.h"
#include "itkRegionOfInterestImageFilter.h"
#include <list>
#include <fstream>
#include <string>
#include <sstream>
#include <iostream>
using namespace std;
#ifndef CACHERESIZEWINDOWLEVEL_H_
#define CACHERESIZEWINDOWLEVEL_H_

#define CACHE_DIRECTORY 1
#define INPUT_DIRECTORY 2
#define INPUT_IMAGE 3
#define MAX_WIDTH 4
#define MAX_HEIGHT 5
#define WINDOW 6
#define LEVEL 7
#define TOP_LEFT_X 8
#define TOP_LEFT_Y 9
#define BOTTOM_RIGHT_X 10
#define BOTTOM_RIGHT_Y 11

#define THUMBNAIL_WIDTH 140
#define THUMBNAIL_HEIGHT 140
#define DEFAULT_WIDTH 720
#define DEFAULT_HEIGHT 768

typedef  short InputPixelType;
typedef itk::RGBPixel< unsigned char >   RGBPixelType;
const unsigned int NDimensions = 2;
typedef itk::Image<InputPixelType, NDimensions>     ImageType;
typedef itk::Image<RGBPixelType , NDimensions > 	RGBImageType;

typedef ImageType::IndexType                   ImageIndexType;
typedef ImageType::Pointer                     ImagePointerType;
typedef ImageType::RegionType                  ImageRegionType;
typedef ImageType::SizeType                    ImageSizeType;

typedef double                  CoordRepType;
typedef itk::AffineTransform<CoordRepType,NDimensions>   AffineTransformType;
typedef itk::LinearInterpolateImageFunction<ImageType,CoordRepType>  InterpolatorType;

typedef itk::ImageFileReader< ImageType > ReaderType;
typedef itk::ImageFileReader< RGBImageType > RGBReaderType;
typedef itk::ImageFileWriter< ImageType > WriterType;
typedef itk::ImageFileWriter< RGBImageType > RGBWriterType;


typedef itk::Image< InputPixelType, 2 > InputImageType;
typedef itk::RegionOfInterestImageFilter< ImageType,ImageType > GrayscaleRegionFilterType;
typedef itk::RegionOfInterestImageFilter< RGBImageType,RGBImageType > RGBRegionFilterType;

typedef itk::LinearInterpolateImageFunction<RGBImageType,CoordRepType>  RGBInterpolatorType;


typedef itk::GDCMImageIO        ImageIOType;

typedef unsigned char WritePixelType;

typedef itk::Image< WritePixelType, 2 > WriteImageType;
typedef itk::Image< RGBPixelType, 2 > RGBWriteImageType;
const unsigned int Dimension = 2;
typedef itk::ResampleImageFilter< ImageType, ImageType > ResampleFilterType;
bool subregionSpecified = false;


		//
/**
 *
 * Basic idea:
 * Given a name, input stream, size (and frame?) and orientation generate a file name for
 * rescaled dicom file. If the file exists, skip and to next step.
 *
 * Goals:
 * Has to work in a streaming environment. The scaled DICOM file must be created
 * as the other file is arriving over standard input; the rest of the pipeline can be generated
 * from that
 * Args
 * target width (original orientation)
 * target height (original orientation)
 * transform (rotation?)
 * image name (SOPInstance works?
 * output mime type
 * and input stream - then
 * - reads input stream
 * - writes output stream
 *
 * -- Future - can AES encode file.
 *
 **/

int writeGreyscalePixels(int, int , int ,int ,char * );
int writeRGBPixels(int, int , int ,int ,char * );
int generateGrayscaleCachefile(ReaderType::Pointer , char *, int ,  double , AffineTransformType::Pointer , InputImageType::SizeType, GrayscaleRegionFilterType::Pointer);
int generateRGBCacheFile(string , char *, int ,  double , AffineTransformType::Pointer , InputImageType::SizeType, RGBRegionFilterType::Pointer );


#endif /*CACHERESIZEWINDOWLEVEL_H_*/
