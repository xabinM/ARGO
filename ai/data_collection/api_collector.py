from typing import List, Dict, Optional
import aiohttp
import asyncio
from dataclasses import dataclass

from config.settings import Settings
from utils.logger import setup_logger

logger = setup_logger(__name__)

@dataclass
class RawLocationData:
    """원시 장소 데이터"""
    source_id: str
    name: str
    raw_location: str
    raw_description: str
    source_api: str
    metadata: Dict

class PublicAPICollector:
    def __init__(self):
        self.settings = Settings()
        
    async def fetch_heritage_data(self) -> List[RawLocationData]:
        """문화재 데이터 수집"""
        # TODO: 구현
        pass
    
    async def fetch_education_curriculum(self) -> List[Dict]:
        """교육과정 데이터 수집"""
        # TODO: 구현  
        pass