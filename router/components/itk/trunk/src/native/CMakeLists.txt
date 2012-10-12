# This project is designed to be built outside the Insight source tree.
PROJECT(FilterTest)

# Find ITK.
FIND_PACKAGE(ITK REQUIRED)
IF(ITK_FOUND)
	INCLUDE(${ITK_USE_FILE})
ENDIF(ITK_FOUND)

ADD_EXECUTABLE(CacheResizeWindowLevel CacheResizeWindowLevel.cpp )

TARGET_LINK_LIBRARIES(CacheResizeWindowLevel ITKIO)
