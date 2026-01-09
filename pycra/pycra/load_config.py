import os
import sys

current_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.abspath(os.path.join(current_dir, '..'))
pycra_config_dir = os.path.join(project_root, 'configs', 'pycra')
sys.path.insert(0, pycra_config_dir)

try:
    from pycra_config import Config
    settings = Config.load_config()

except ImportError as e:
    import importlib.util
    spec = importlib.util.spec_from_file_location("settings", os.path.join(pycra_config_dir, "settings.py"))
    pycra_config = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(pycra_config)
    settings = pycra_config.Config.load_config()