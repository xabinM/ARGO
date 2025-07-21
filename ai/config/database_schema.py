# 스키마 초안(수정 필요)
from pydantic import BaseModel

class LocationSpot(BaseModel):
    id: str
    name: str
    latitude: float  
    longitude: float
    content: str
    parent_name: Optional[str]
    location: Optional[str]